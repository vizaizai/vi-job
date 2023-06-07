package com.github.vizaizai.worker.core.processor;


import com.github.vizaizai.common.model.TaskTriggerParam;
import com.github.vizaizai.worker.core.TaskContext;

/**
 * 基础处理器
 * @author liaochongwei
 * @date 2023/4/23 10:16
 */
public interface BasicProcessor {
    /**
     * 执行处理器
     * @param taskContext 任务上下文
     * @return TaskResult
     */
    void execute(TaskContext taskContext) throws Exception;
}
