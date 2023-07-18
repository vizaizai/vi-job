package com.github.vizaizai.worker.core.executor;

import com.github.vizaizai.common.model.LogQueryParam;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import com.github.vizaizai.worker.log.impl.JobLogger;
import com.github.vizaizai.worker.utils.DateUtils;

/**
 * 执行日志
 * @author liaochongwei
 * @date 2023/5/23 15:56
 */
public class LogExecutor implements BizProcessor {
    @Override
    public void execute(RpcRequest request, Sender sender) {
        LogQueryParam param = (LogQueryParam) request.getParam();
        RpcResponse response;
        JobLogger jobLogger = null;
        try {
            jobLogger = JobLogger.getInstance(param.getJobId(), DateUtils.parse(param.getTriggerTime()).toLocalDate(), false);
            response = RpcResponse.ok(jobLogger.getLog(param.getLogId(), param.getStartPos(), param.getMaxLines()));
        }catch (Exception e) {
            response = RpcResponse.error(e.getMessage());
        }finally {
            if (jobLogger != null) {
                jobLogger.close();
            }
        }
        sender.send(RpcMessage.createResponse(request.getRid(), response));
    }
}
