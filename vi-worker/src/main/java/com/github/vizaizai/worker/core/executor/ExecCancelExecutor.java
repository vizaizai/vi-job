package com.github.vizaizai.worker.core.executor;

import com.github.vizaizai.common.model.ExecCancelParam;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import com.github.vizaizai.worker.runner.JobProcessRunner;

/**
 * 取消待执行任务
 * @author liaochongwei
 * @date 2023/6/23 15:56
 */
public class ExecCancelExecutor implements BizProcessor {
    @Override
    public void execute(RpcRequest request, Sender sender) {
        ExecCancelParam param = (ExecCancelParam) request.getParam();
        RpcResponse response;
        try {
            JobProcessRunner runner = JobProcessRunner.getRunner(param.getJobId());
            if (runner != null) {
                response = RpcResponse.ok(runner.cancel(param.getJobDispatchId()));
            }else {
                response = RpcResponse.ok(false);
            }
        }catch (Exception e) {
            response = RpcResponse.error(e.getMessage());
        }
        sender.send(RpcMessage.createResponse(request.getRid(), response));
    }
}
