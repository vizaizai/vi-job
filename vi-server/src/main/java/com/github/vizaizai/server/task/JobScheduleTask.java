package com.github.vizaizai.server.task;

import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.service.GlobalJobGroupManager;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * job计划定时任务
 * @author liaochongwei
 * @date 2023/5/31 11:10
 */
@Slf4j
@Component
public class JobScheduleTask extends BaseTask{
    @Resource
    private JobService jobService;
    @Resource
    private GlobalJobGroupManager globalJobGroupManager;
    /**
     * 每分钟将5分钟内触发的任务添加到触发Timer中
     */
    @Scheduled(fixedDelay = 1000 * 60)
    public void schedule() {
        if (!this.execTask()) {
            return;
        }
        List<JobDO> jobs = jobService.listWaitingJobs(System.currentTimeMillis() + Commons.TIMER_MAX);
        if (Utils.isEmpty(jobs)) {
            return;
        }
        for (JobDO jobDO : jobs) {
            try {
                globalJobGroupManager.pushIntoTimer(BeanUtils.toBean(jobDO, Job::new));
            }catch (Exception e) {
                log.error("任务【{}】推入定时器失败：",jobDO.getId(), e);
            }
        }
    }
}
