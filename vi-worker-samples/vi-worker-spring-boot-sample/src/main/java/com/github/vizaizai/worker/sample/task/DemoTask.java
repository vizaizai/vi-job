package com.github.vizaizai.worker.sample.task;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.worker.core.TaskContext;
import com.github.vizaizai.worker.core.annotation.Job;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

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
    public void bar(TaskContext context) {
        logger.info("开始执行定时任务bar...");
        try {
            context.getLogger().info("demoTask2开始执行咯");
            Thread.sleep(1000 * 2);
            for (int i = 0; i < 1000; i++) {
                context.getLogger().info("demoTask2执行中打印: {}", i);
            }
        }catch (Exception e) {
        }
        logger.info("执行订单任务完毕...");

        int nextInt = ThreadLocalRandom.current().nextInt(0, 4);
        if (nextInt==1) {
            throw new RuntimeException("执行失败，nextInt=1");
        }
    }

    @Job("demoTask3")
    public void demoTask3(TaskContext context) {
        logger.info("开始执行定时任务bar...");
        try {
            context.getLogger().info("demoTask3开始执行咯");
        }catch (Exception e) {
        }
        throw new RuntimeException("执行失败,哈哈哈哈哈哈");
    }
}
