package com.github.vizaizai.worker.core.processor.method;

import com.github.vizaizai.worker.core.processor.BasicProcessor;

import java.util.Map;

/**
 * 任务方法解析器
 * @author liaochongwei
 * @date 2023/4/25 15:20
 */
public interface JobMethodResolver {

    Map<String, BasicProcessor> resolve();
}
