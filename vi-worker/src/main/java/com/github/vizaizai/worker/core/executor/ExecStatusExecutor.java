package com.github.vizaizai.worker.core.executor;

import com.github.vizaizai.common.contants.ExtendExecStatus;
import com.github.vizaizai.common.model.ExecStatusQueryParam;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import com.github.vizaizai.worker.runner.JobProcessRunner;

/**
 * 查询执行状态
 * @author liaochongwei
 * @date 2023/6/23 15:56
 */
public class ExecStatusExecutor implements BizProcessor {
    @Override
    public void execute(RpcRequest request, Sender sender) {
        ExecStatusQueryParam param = (ExecStatusQueryParam) request.getParam();
        RpcResponse response;
        try {
            JobProcessRunner runner = JobProcessRunner.getRunner(param.getJobId());
            if (runner != null) {
                response = RpcResponse.ok(runner.status(param.getJobInstanceId()));
            }else {
                response = RpcResponse.ok(ExtendExecStatus.UNKNOWN.getCode());
            }
        }catch (Exception e) {
            response = RpcResponse.error(e.getMessage());
        }
        sender.send(RpcMessage.createResponse(request.getRid(), response));
    }
}
