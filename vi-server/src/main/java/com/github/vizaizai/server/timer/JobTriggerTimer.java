package com.github.vizaizai.server.timer;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.github.vizaizai.retry.timewheel.HashedWheelTimer;
import com.github.vizaizai.retry.timewheel.Timeout;
import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.constant.TriggerType;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.timer.watch.WatchDogRunner;
import com.github.vizaizai.server.timer.watch.WatchInstance;
import com.github.vizaizai.server.utils.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.system.SystemProperties;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 任务触发器Timer
 * @author liaochongwei
 * @date 2023/5/18 20:50
 */
@Slf4j
public class JobTriggerTimer {

    /**
     * 任务执行线程池
     */
    private final ExecutorService invokeExecutor;
    /**
     * 慢时间轮
     */
    private final HashedWheelTimer slowHashedWheelTimer;
    /***
     * 快时间轮
     */
    private final HashedWheelTimer fastHashedWheelTimer;

    private final Map<String,Timeout> timeouts = new ConcurrentHashMap<>();
    /**
     * 调度中的任务
     */
    private final static Map<Long, Job> schedulingJobs = new ConcurrentHashMap<>();
    private static JobService jobService  = null;

    private JobTriggerTimer() {
        slowHashedWheelTimer = new HashedWheelTimer(500L, 1024, 0, 1,-1);
        fastHashedWheelTimer = new HashedWheelTimer(10L, 4096, 1, Runtime.getRuntime().availableProcessors() * 2, -1);

        String size = SystemProperties.get("job.trigger.maxPoolSize");
        int poolSize = StringUtils.isNotBlank(size) ? Integer.parseInt(size) : 200;
        invokeExecutor = new ThreadPoolExecutor(
                10,
                poolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2000),
                new BasicThreadFactory.Builder().namingPattern("Trigger-task-%d").build());

        log.info(">>>>>>>>>>Trigger timer started");

    }

    private static final class JobTriggerTimerHolder {
        static final JobTriggerTimer jobTriggerTimer = new JobTriggerTimer();
    }

    public static synchronized JobTriggerTimer getInstance() {
        if (jobService == null) {
            jobService = ContextUtil.getBean(JobService.class);
        }
        return JobTriggerTimerHolder.jobTriggerTimer;
    }


    /**
     * 将job推入到Timer
     * @param job 任务实体
     */
    public synchronized void push(Job job) {
        String timeoutId = this.getTimeoutId(job.getId(), job.getInstanceId());
        // 任务重复调度
        Timeout timeout = getTimeout(timeoutId);
        if (timeout!= null && !timeout.isExpired() && !timeout.isCancelled()) {
            return;
        }
        // 延时任务校验，保证上一次调度执行结束
        if (Objects.equals(job.getTriggerType(), TriggerType.DELAYED.getCode())) {
            if (WatchDogRunner.getInstance().isRunning(WatchInstance.getWatchId1(job.getId()))) {
                return;
            }
        }

        // 生命周期检查
        if (job.getEndTime() != null
                && System.currentTimeMillis() > LocalDateTimeUtil.toEpochMilli(job.getEndTime())) {
            invokeExecutor.execute(()-> jobService.stop(job.getId()));
            remove(job.getId());
            return;
        }

        // 触发时间检查
        Long triggerTime = job.getNextTriggerTime();
        if (triggerTime == null) {
            return;
        }

        // 延时毫秒数
        long delay = triggerTime - System.currentTimeMillis();
        if (delay > Commons.TIMER_MAX) {
            return;
        }
        // 任务过期
        if (delay < 0) {
            // 直接调度任务：运行一次
            if (job.isDirectRun()) {
                new JobInvoker(invokeExecutor, jobService, job).exec();
                return;
            }
            // 定时调度任务：计算下次调度时间
            job.setBaseTime(System.currentTimeMillis());
            job.resetNextTriggerTime();
            this.push(job);
            return;
        }
        if (!job.isDirectRun()) {
            schedulingJobs.put(job.getId(), job);
        }
        // 延时调度
        this.schedule(delay, timeoutId, new JobInvoker(invokeExecutor, jobService, job));
    }


    /**
     * 定时任务
     * @param delay 延时毫秒数
     * @param timeoutId timeout标识
     * @param jobInvoker JobInvoker
     */
    private void schedule(long delay, String timeoutId, JobInvoker jobInvoker) {
        // 延时时间大于最小时间->推入慢时间轮循环
        if (delay > Commons.TIMER_MIN) {
            Timeout timeout = slowHashedWheelTimer.newTimeout(()-> {
                timeouts.remove(timeoutId);
                // 再次schedule，进入快时间轮
                this.schedule(Commons.TIMER_MIN, timeoutId, jobInvoker);
            }
            ,delay - Commons.TIMER_MIN
            ,TimeUnit.MILLISECONDS);
            // 将Timeout记录，用于中止任务
            timeouts.put(timeoutId, timeout);
        }else if (delay > 50L) {
            Timeout timeout = fastHashedWheelTimer.newTimeout(()->{
                timeouts.remove(timeoutId);
                // 运行任务
                jobInvoker.exec();
            }
            ,delay
            ,TimeUnit.MILLISECONDS);
            timeouts.put(timeoutId, timeout);
        }else  {
            // 运行任务
            jobInvoker.exec();
        }
    }
    /**
     * 从Timer中移除job
     * @param jobId 任务id
     */
    public synchronized void remove(Long jobId) {
        Timeout timeout;
        timeout = timeouts.remove(jobId.toString());
        if (timeout != null) {
            timeout.cancel();
        }
        Job job = schedulingJobs.remove(jobId);
        if (job != null) {
            job.setCanceled(true);
        }
    }

    public HashedWheelTimer getSlowHashedWheelTimer() {
        return slowHashedWheelTimer;
    }

    public HashedWheelTimer getFastHashedWheelTimer() {
        return fastHashedWheelTimer;
    }

    public Timeout getTimeout(String timeoutId) {
        return timeouts.get(timeoutId);
    }

    public String getTimeoutId(Long jobId, Long jobInstanceId) {
        String timeoutId = String.valueOf(String.valueOf(jobId));
        if (jobInstanceId != null) {
            timeoutId = timeoutId + "_" + jobInstanceId;
        }
        return timeoutId;
    }

}
