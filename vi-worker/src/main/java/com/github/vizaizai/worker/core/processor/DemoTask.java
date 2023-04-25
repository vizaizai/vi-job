package com.github.vizaizai.worker.core.processor;

import com.github.vizaizai.common.model.TaskContext;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.worker.core.annotation.Job;
import org.slf4j.Logger;

/**
 * @author liaochongwei
 * @date 2023/4/25 14:16
 */
public class DemoTask {
    private static final Logger logger = LoggerFactory.getLogger(DemoTask.class);

    @Job("testJob")
    private void job1(TaskContext taskContext) {
        logger.info("开始执行定时任务: {}",taskContext.getJobId());
        try {
            Thread.sleep(1000 * 10);
        }catch (Exception e) {

        }
        logger.info("执行定时任务结束");
    }
}
