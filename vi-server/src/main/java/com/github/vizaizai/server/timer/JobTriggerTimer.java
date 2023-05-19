package com.github.vizaizai.server.timer;

import com.github.vizaizai.common.model.TaskContext;
import com.github.vizaizai.remote.client.NettyPoolClient;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.BizCode;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.utils.NetUtils;
import com.github.vizaizai.retry.timewheel.HashedWheelTimer;
import com.github.vizaizai.retry.timewheel.Timeout;
import com.github.vizaizai.server.entity.Job;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.system.SystemProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 任务触发器Timer
 * @author liaochongwei
 * @date 2023/5/18 20:50
 */
public class JobTriggerTimer {
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
        String slowSize = SystemProperties.get("wheelTimer.slow.maxPoolSize");
        int slowPoolSize = StringUtils.isNotBlank(slowSize) ? Integer.parseInt(slowSize) : 100;

        String fastSize = SystemProperties.get("wheelTimer.fast.maxPoolSize");
        int fastPoolSize = StringUtils.isNotBlank(fastSize) ? Integer.parseInt(fastSize) : 200;

        slowHashedWheelTimer = new HashedWheelTimer(1000L, 1024, 0, slowPoolSize,-1);
        fastHashedWheelTimer = new HashedWheelTimer(100L, 4096, Runtime.getRuntime().availableProcessors() * 2, fastPoolSize, -1);


    }

    private static final class JobTriggerTimerHolder {
        static final JobTriggerTimer jobTriggerTimer = new JobTriggerTimer();
    }

    public static synchronized JobTriggerTimer getInstance() {
        return JobTriggerTimerHolder.jobTriggerTimer;
    }


    public void addTrigger(Job job) {
        Long triggerTime = job.calculateNextTriggerTime();
        if (triggerTime == null) {
            return;
        }
        // 等待毫秒数
        long waitMs = triggerTime - System.currentTimeMillis();
        // 等待10分钟以上推入慢时间轮循环
        if (waitMs > 600000) {

            Timeout timeout = slowHashedWheelTimer.newTimeout(()-> {
                this.addTrigger(job);
                slowTimeouts.remove(job.getId());
            }, waitMs - 600000, TimeUnit.MILLISECONDS);

            // 将Timeout缓存下来，用与中止任务
            slowTimeouts.put(job.getId(),timeout);
        }else if (waitMs > 100L) {
            Timeout timeout = fastHashedWheelTimer.newTimeout(new FastTriggerTimerTask(job), waitMs, TimeUnit.MILLISECONDS);
            // 将Timeout缓存下来，用户中止任务
            fastTimeouts.put(job.getId(),timeout);
        }else {
            // 直接触发
        }

    }

    private void invoke(Job job) {
        String address = job.getWorkerAddressList().get(0);
        Pair<String, Integer> netPair = NetUtils.splitAddress2IpAndPort(address);
        TaskContext taskContext = new TaskContext();
        taskContext.setJobId(job.getId());
        taskContext.setJobName(job.getName());
        taskContext.setJobDispatchId("1111");
        taskContext.setJobParams(job.getParam());
        taskContext.setExecuteTimeout(3);
        RpcResponse rpcResponse = NettyPoolClient.getInstance(netPair.getKey(), netPair.getValue()).request(RpcRequest.wrap(BizCode.RUN, taskContext), 30000);
    }

    public HashedWheelTimer getSlowHashedWheelTimer() {
        return slowHashedWheelTimer;
    }

    public HashedWheelTimer getFastHashedWheelTimer() {
        return fastHashedWheelTimer;
    }

    public Map<String, Timeout> getSlowTimeouts() {
        return slowTimeouts;
    }

    public Map<String, Timeout> getFastTimeouts() {
        return fastTimeouts;
    }

}
