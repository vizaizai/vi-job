package com.github.vizaizai.worker.core.executor;

import com.github.vizaizai.common.model.TaskResult;
import com.github.vizaizai.common.model.TaskTriggerParam;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.worker.core.TaskContext;
import com.github.vizaizai.worker.core.processor.BasicProcessor;
import com.github.vizaizai.worker.runner.JobProcessRunner;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Processor执行器
 * @author liaochongwei
 * @date 2023/4/23 10:18
 */
public class TaskExecutor implements BizProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutor.class);
    /**
     * 处理器列表
     */
    private static final Map<String, BasicProcessor> processors = new HashMap<>();
    /**
     * 注入任务处理器
     * @param jobName 任务名称
     * @param processor 处理器
     */
    public static void register(String jobName, BasicProcessor processor) {
        if (processors.get(jobName) == null) {
            processors.put(jobName,processor);
        }else {
            throw new RuntimeException("JobName[" + jobName + "] must be unique.") ;
        }
    }

    /**
     * 注入任务处理器
     * @param processorMap 处理器列表
     */
    public static void register(Map<String, BasicProcessor> processorMap) {
        if (Utils.isNotEmpty(processorMap)) {
            processorMap.forEach(TaskExecutor::register);
        }

    }

    public Map<String, BasicProcessor> getProcessors() {
        return processors;
    }

    @Override
    public void execute(RpcRequest request, Sender sender) {
        TaskTriggerParam triggerParam = (TaskTriggerParam) request.getParam();
        TaskResult result;
        // 执行处理器
        BasicProcessor basicProcessor = processors.get(triggerParam.getJobName());
        if (basicProcessor != null) {
            // 推入待执行任务队列中
            TaskContext taskContext = new TaskContext(triggerParam, sender);
            JobProcessRunner runner = JobProcessRunner.getInstance(triggerParam.getJobId(), basicProcessor);
            result = runner.pushTaskQueue(taskContext);
        }else {
            result = TaskResult.fail("Processor not found");
        }
        // 响应客户端
        sender.send(RpcMessage.createResponse(request.getRid(),RpcResponse.ok(result)));
    }
}
