package com.github.vizaizai.server.task;

import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.service.WorkerService;
import com.github.vizaizai.server.timer.JobTriggerTimer;
import com.github.vizaizai.server.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * job检查任务
 * @author liaochongwei
 * @date 2023/5/31 11:10
 */
@Slf4j
@Component
public class JobCheckTask {

    @Resource
    private JobService jobService;
    @Resource
    private WorkerService workerService;
    /**
     * 3分钟执行一次,将5分钟内触发的任务添加到触发Timer中
     */
    @Scheduled(fixedDelay = 1000 * 60 * 3)
    public void check() {
        List<JobDO> jobs = jobService.listWaitingJobs(System.currentTimeMillis() + Commons.TIMER_MAX);
        if (Utils.isEmpty(jobs)) {
            return;
        }
        JobTriggerTimer jobTriggerTimer = JobTriggerTimer.getInstance();
        for (JobDO jobDO : jobs) {
            Job job = BeanUtils.toBean(jobDO, Job::new);
            job.setWorkerAddressList(workerService.getWorkerAddressList(job.getWorkerId()));
            if (jobTriggerTimer.getTimeout(job.getId()) == null) {
                jobTriggerTimer.addToTimer(job);
            }

        }
    }
}
