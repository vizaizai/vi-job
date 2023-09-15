package com.github.vizaizai.server.service;

import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.common.model.StatusReportParam;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import com.github.vizaizai.server.utils.BeanUtils;
import com.github.vizaizai.server.web.co.StatusReportCO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 任务上报处理器
 * @author liaochongwei
 * @date 2023/6/7 14:57
 */
@Slf4j
public class JobReportProcessor implements BizProcessor {
    private final JobService jobService;

    public JobReportProcessor(JobService jobService) {
        this.jobService = jobService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(RpcRequest request, Sender sender) {
        List<StatusReportParam> reportParams = (List<StatusReportParam>) request.getParam();
        List<StatusReportCO> statusReportList = BeanUtils.toBeans(reportParams, StatusReportCO::new);
        try {
            Result<Void> result = jobService.statusReport(statusReportList);
            sender.send(RpcMessage.createResponse(request.getRid(), RpcResponse.ok(result)));
        }catch (Exception e) {
            log.error("批量处理状态上报失败,",e);
            sender.send(RpcMessage.createResponse(request.getRid(), RpcResponse.error(e.getMessage())));
        }
    }
}
