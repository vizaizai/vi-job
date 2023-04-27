package com.github.vizaizai.worker.sample.task;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.worker.core.annotation.Job;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * @author liaochongwei
 * @date 2023/4/26 10:39
 */
@Component
public class DemoTask {
    private static final Logger logger = LoggerFactory.getLogger(DemoTask.class);

    @Job("demoTask1")
    public void foo() {

        logger.info("开始执行定时任务foo...");
        try {
            Thread.sleep(1000 * 10);
        }catch (Exception e) {

        }
        logger.info("执行订单任务完毕...");

    }
    @Job("demoTask2")
    public void bar() {

        logger.info("开始执行定时任务bar...");
        try {
            Thread.sleep(1000 * 2);
        }catch (Exception e) {

        }
        logger.info("执行订单任务完毕...");

    }
}
