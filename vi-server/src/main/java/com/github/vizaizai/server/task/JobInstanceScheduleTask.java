package com.github.vizaizai.server.task;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.dao.dataobject.JobInstanceDO;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.service.GlobalJobGroupManager;
import com.github.vizaizai.server.service.JobInstanceService;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * job实例计划任务
 * @author liaochongwei
 * @date 2023/5/31 11:10
 */
@Slf4j
@Component
public class JobInstanceScheduleTask extends BaseTask{
    @Resource
    private JobService jobService;
    @Resource
    private JobInstanceService jobInstanceService;
    @Resource
    private GlobalJobGroupManager globalJobGroupManager;
    /**
     * 每分钟将5分钟内待触发的任务实例添加到触发Timer中
     */
    @Scheduled(fixedDelay = 1000 * 60)
    public void schedule() {
        if (!this.execTask()) {
            return;
        }
        List<JobInstanceDO> jobInstances = jobInstanceService.listWaitingInstances(System.currentTimeMillis() + Commons.TIMER_MAX);
        if (Utils.isEmpty(jobInstances)) {
            return;
        }

        Map<Long, Job> jobMap = new HashMap<>();
        List<List<Long>> cuts = Utils.cutList(jobInstances.stream().map(JobInstanceDO::getJobId).distinct().collect(Collectors.toList()), 50);
        for (List<Long> cutItem : cuts) {
            List<JobDO> jobs = jobService.listByIds(cutItem);
            for (JobDO job : jobs) {
                jobMap.put(job.getId(), BeanUtils.toBean(job, Job::new));
            }
        }

        for (JobInstanceDO jobInstance : jobInstances) {
            try {
                Job job = BeanUtils.toBean(jobMap.get(jobInstance.getJobId()), Job::new);
                job.setNextTriggerTime(LocalDateTimeUtil.toEpochMilli(jobInstance.getTriggerTime()));
                job.setInstanceId(jobInstance.getId());
                job.setDirectRun(true);
                globalJobGroupManager.pushIntoTimer(job);
            }catch (Exception e) {
                log.error("任务实例【{}】推入定时器失败：",jobInstance.getId(), e);
            }
        }

    }
}
