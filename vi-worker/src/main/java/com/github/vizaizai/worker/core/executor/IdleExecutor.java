package com.github.vizaizai.worker.core.executor;

import com.github.vizaizai.common.model.TaskResult;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import com.github.vizaizai.worker.core.processor.JobProcessorRunner;

/**
 * 空闲检测
 * @author liaochongwei
 * @date 2023/5/23 15:56
 */
public class IdleExecutor implements BizProcessor {
    @Override
    public void execute(RpcRequest request, Sender sender) {
        String jobId = (String) request.getParam();
        JobProcessorRunner runner = JobProcessorRunner.getRunner(jobId);
        RpcResponse response;
        if (runner == null || !runner.isRunning()) {
            response = RpcResponse.ok(request.getRequestId(), TaskResult.ok());
        }else {
            response = RpcResponse.ok(request.getRequestId(), TaskResult.fail("Job["+jobId+"] is busy"));
        }
        sender.send(response);
    }
}
