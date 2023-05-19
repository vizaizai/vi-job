package com.github.vizaizai.server.timer;

import com.github.vizaizai.retry.timewheel.HashedWheelTimer;
import com.github.vizaizai.retry.timewheel.TimerTask;
import com.github.vizaizai.server.entity.Job;

/**
 * 快时间轮Task
 * @author liaochongwei
 * @date 2023/5/19 15:48
 */
public class FastTriggerTimerTask implements TimerTask {

    private final Job job;

    public FastTriggerTimerTask(Job job) {
        this.job = job;

    }

    @Override
    public void run() {

    }


}
