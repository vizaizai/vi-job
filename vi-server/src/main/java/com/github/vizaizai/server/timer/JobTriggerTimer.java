package com.github.vizaizai.server.timer;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.github.vizaizai.retry.timewheel.HashedWheelTimer;
import com.github.vizaizai.retry.timewheel.Timeout;
import com.github.vizaizai.server.constant.TriggerType;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.utils.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.system.SystemProperties;

import java.time.LocalDateTime;
import java.util.Map;
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

    private final Map<String,Timeout>  slowTimeouts = new ConcurrentHashMap<>();
    private final Map<String,Timeout>  fastTimeouts = new ConcurrentHashMap<>();


    private JobTriggerTimer() {
        slowHashedWheelTimer = new HashedWheelTimer(1000L, 1024, 0, 1,-1);
        fastHashedWheelTimer = new HashedWheelTimer(100L, 4096, 1, Runtime.getRuntime().availableProcessors() * 2, -1);

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
        return JobTriggerTimerHolder.jobTriggerTimer;
    }


    public void pushTimer(Job job) {
        Long triggerTime = job.getNextTriggerTime();
        if (triggerTime == null) {
            return;
        }
        // 等待毫秒数
        long waitMs = triggerTime - System.currentTimeMillis();
        // 等待10分钟以上推入慢时间轮循环
        if (waitMs > 600000) {
            Timeout timeout = slowHashedWheelTimer.newTimeout(()-> {
                this.pushTimer(job);
                slowTimeouts.remove(job.getId());
            }, waitMs - 600000, TimeUnit.MILLISECONDS);

            // 将Timeout缓存下来，用于中止任务
            slowTimeouts.put(job.getId(),timeout);
        }else if (waitMs > 100L) {
            Timeout timeout = fastHashedWheelTimer.newTimeout(()->{
                // 调度任务
                this.invoke(job);
            }, waitMs, TimeUnit.MILLISECONDS);
            fastTimeouts.put(job.getId(),timeout);
        }else {
            // 直接触发
            this.invoke(job);
        }

    }

    private void invoke(Job job) {
        job.setLastTriggerTime(LocalDateTimeUtil.now());
        // 若触发类型非固定延时,则重新推入timer中
        if (job.getTriggerType() != TriggerType.DELAYED.getCode()) {
            pushTimer(job);
        }
        invokeExecutor.execute(() -> {
            try {
                ContextUtil.getBean(JobService.class).invoke(job);
            }catch (Exception e) {
                log.error("Job execute error.", e);
            }
        });

    }

    public HashedWheelTimer getSlowHashedWheelTimer() {
        return slowHashedWheelTimer;
    }

    public HashedWheelTimer getFastHashedWheelTimer() {
        return fastHashedWheelTimer;
    }

    public Timeout getTimeout(String jobId) {
        if (slowTimeouts.containsKey(jobId)) {
            return slowTimeouts.get(jobId);
        }
        return fastTimeouts.get(jobId);

    }

    public Map<String, Timeout> getSlowTimeouts() {
        return slowTimeouts;
    }

    public Map<String, Timeout> getFastTimeouts() {
        return fastTimeouts;
    }

}
