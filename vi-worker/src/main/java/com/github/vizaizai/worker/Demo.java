package com.github.vizaizai.worker;

import com.github.vizaizai.common.model.TaskContext;
import com.github.vizaizai.common.model.TaskResult;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.worker.core.executor.TaskExecutor;
import com.github.vizaizai.worker.core.processor.BasicProcessor;
import com.github.vizaizai.worker.core.processor.DemoTask;
import com.github.vizaizai.worker.core.processor.method.SimpleMethodResolver;
import com.github.vizaizai.worker.starter.ViStarter;
import org.slf4j.Logger;

import java.util.Arrays;

/**
 * @author liaochongwei
 * @date 2023/4/24 15:18
 */
public class Demo {
    private static final Logger logger = LoggerFactory.getLogger(Demo.class);
    public static void main(String[] args) {
        TaskExecutor.register(new SimpleMethodResolver(Arrays.asList(new DemoTask())).resolve());
        ViStarter viJobStarter = new ViStarter();
        viJobStarter.setHost("127.0.0.1");
        viJobStarter.setPort(7070);
        viJobStarter.start();
    }
}
