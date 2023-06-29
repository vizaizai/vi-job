package com.github.vizaizai.server.task;

import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.raft.RaftServer;
import com.github.vizaizai.server.service.GlobalJobGroupManager;
import com.github.vizaizai.server.service.JobAllocationService;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.timer.JobTriggerTimer;
import com.github.vizaizai.server.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * job计划任务
 * @author liaochongwei
 * @date 2023/5/31 11:10
 */
@Slf4j
@Component
public class JobScheduleTask {

    @Resource
    private JobService jobService;
    @Resource
    private RaftServer raftServer;
    @Resource
    private JobAllocationService jobAllocationService;
    @Resource
    private GlobalJobGroupManager globalJobGroupManager;
    /**
     * 3分钟执行一次,将5分钟内触发的任务添加到触发Timer中
     */
    @Scheduled(fixedDelay = 1000 * 60 * 3)
    public void load() {
        List<JobDO> jobs = jobService.listWaitingJobs(System.currentTimeMillis() + Commons.TIMER_MAX);
        if (Utils.isEmpty(jobs)) {
            return;
        }
        JobTriggerTimer jobTriggerTimer = JobTriggerTimer.getInstance();
        for (JobDO jobDO : jobs) {
            boolean flag = false;
            if (!raftServer.isCluster()) {
                flag = true;
            }else {
                raftServer.waitingToStart();
                String nodeAddress = jobAllocationService.get(jobDO.getId());
                // 任务分组缺失
                if (nodeAddress == null) {
                    try {
                        log.info("任务【{}】分组缺失，执行选择",jobDO.getId());
                        globalJobGroupManager.elect(jobDO.getId());
                    }catch (Exception ignored) {
                    }
                }
                if (Objects.equals(raftServer.getCurrentNodeAddress(), nodeAddress)) {
                    flag = true;
                }
            }
            if (!flag) {
                continue;
            }
            Job job = BeanUtils.toBean(jobDO, Job::new);
            if (jobTriggerTimer.getTimeout(job.getId()) == null) {
                jobTriggerTimer.push(job);
            }
        }
    }
}
