package com.github.vizaizai.worker.core.executor;

import com.github.vizaizai.common.model.TaskContext;
import com.github.vizaizai.common.model.TaskResult;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.worker.core.processor.BasicProcessor;
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
        processors.put(jobName,processor);
    }

    /**
     * 注入任务处理器
     * @param processorMap 处理器列表
     */
    public static void register(Map<String, BasicProcessor> processorMap) {
        if (Utils.isNotEmpty(processorMap)) {
            processors.putAll(processorMap);
        }

    }

    public Map<String, BasicProcessor> getProcessors() {
        return processors;
    }

    @Override
    public void execute(RpcRequest request, Sender sender) {
        TaskContext taskContext = (TaskContext) request.getParam();
        TaskResult result;
        // 执行处理器
        try {
            BasicProcessor basicProcessor = processors.get(taskContext.getJobName());
            if (basicProcessor != null) {
                basicProcessor.execute(taskContext);
                result = TaskResult.ok();
            }else {
                result = TaskResult.fail("Processor not found");
            }
        }catch (Exception e) {
            logger.error("Execute '" + taskContext.getJobName() +"' error,",e);
            result = TaskResult.fail(e.getMessage());
        }
        RpcResponse response = RpcResponse.ok(request.getRequestId(), result);
        sender.send(response);
    }
}
