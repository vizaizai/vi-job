package com.github.vizaizai.server.service;

import com.github.vizaizai.common.model.JobRunParam;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务运行处理器
 * @author liaochongwei
 * @date 2023/6/7 14:57
 */
@Slf4j
public class JobRunProcessor implements BizProcessor {
    private final JobService jobService;

    public JobRunProcessor(JobService jobService) {
        this.jobService = jobService;
    }

    @Override
    public void execute(RpcRequest request, Sender sender) {
        JobRunParam runParam = (JobRunParam) request.getParam();
        try {
            Result<Long> result = jobService.run(runParam);
            sender.send(RpcMessage.createResponse(request.getRid(), RpcResponse.ok(result)));
        }catch (Exception e) {
            log.error("处理任务运行失败,",e);
            sender.send(RpcMessage.createResponse(request.getRid(), RpcResponse.error(e.getMessage())));
        }
    }
}
