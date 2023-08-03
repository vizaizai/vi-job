package com.github.vizaizai.worker.core.executor;

import com.github.vizaizai.common.model.TaskResult;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import com.github.vizaizai.worker.runner.JobProcessRunner;

/**
 * 空闲检测
 * @author liaochongwei
 * @date 2023/5/23 15:56
 */
public class IdleExecutor implements BizProcessor {
    @Override
    public void execute(RpcRequest request, Sender sender) {
        Long jobId = (Long) request.getParam();
        JobProcessRunner runner = JobProcessRunner.getRunner(jobId);
        RpcResponse response;
        if (runner == null || !runner.isRunning()) {
            response = RpcResponse.ok(TaskResult.ok());
        }else {
            response = RpcResponse.ok(TaskResult.fail("Job["+jobId+"] is busy"));
        }
        sender.send(RpcMessage.createResponse(request.getRid(),response));
    }
}
