package com.github.vizaizai.worker.sample.controller;

import com.github.vizaizai.logging.LoggerFactory;
import org.slf4j.Logger;

/**
 * @author liaochongwei
 * @date 2023/7/12 14:29
 */
public class Demo {
    private static final Logger logger = LoggerFactory.getLogger(Demo.class);
    public static void main(String[] args) {

        long st = System.currentTimeMillis();
        for (int i = 1; i < 5; i++) {
            logger.info("这是第{}条日志", i);
        }
        System.out.println("耗时:" + (System.currentTimeMillis() - st));
    }
}
