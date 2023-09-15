package com.github.vizaizai.worker.core.executor;

import com.github.vizaizai.common.model.LogQueryParam;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import com.github.vizaizai.worker.log.impl.JobLoggerHandler;
import com.github.vizaizai.worker.utils.DateUtils;

/**
 * 查询执行日志
 * @author liaochongwei
 * @date 2023/6/23 15:56
 */
public class LogExecutor implements BizProcessor {
    @Override
    public void execute(RpcRequest request, Sender sender) {
        LogQueryParam param = (LogQueryParam) request.getParam();
        RpcResponse response;
        JobLoggerHandler jobLoggerHandler = null;
        try {
            jobLoggerHandler = JobLoggerHandler.getInstance(param.getJobId(), DateUtils.parse(param.getTriggerTime()).toLocalDate(), false);
            response = RpcResponse.ok(jobLoggerHandler.getLog(param.getLogId(), param.getStartPos(), param.getMaxLines()));
        }catch (Exception e) {
            e.printStackTrace();
            response = RpcResponse.error(e.getMessage());
        }finally {
            if (jobLoggerHandler != null) {
                jobLoggerHandler.close();
            }
        }
        sender.send(RpcMessage.createResponse(request.getRid(), response));
    }
}
