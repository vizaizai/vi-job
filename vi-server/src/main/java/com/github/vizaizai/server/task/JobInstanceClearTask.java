package com.github.vizaizai.server.task;

import com.github.vizaizai.server.service.JobInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 任务实例清理任务
 * @author liaochongwei
 * @date 2023/5/8 17:12
 */
@Slf4j
@Component
public class JobInstanceClearTask extends BaseTask {

    @Resource
    private JobInstanceService jobInstanceService;
    /**
     * 30分钟执行一次
     */
    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void check() {
        if (!this.execTask()) {
           return;
        }
        int count = jobInstanceService.batchRemove();
        if (count > 0) {
            log.info("{} record was cleared", count);
        }

    }
}
