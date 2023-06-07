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

/**
 * 任务上报处理器
 * @author liaochongwei
 * @date 2023/6/7 14:57
 */
@Slf4j
@Service
public class JobReportProcessor implements BizProcessor {
    @Resource
    private JobService jobService;
    @Override
    public void execute(RpcRequest request, Sender sender) {
        StatusReportParam reportParam = (StatusReportParam) request.getParam();
        StatusReportCO statusReportCO = BeanUtils.toBean(reportParam, StatusReportCO::new);
        try {
            log.info(">>>>>>>>>>开始处理状态上报");
            Result<Void> result = jobService.statusReport(statusReportCO);
            sender.send(RpcMessage.createResponse(request.getRid(), RpcResponse.ok(result)));
        }catch (Exception e) {
            log.error("处理状态上报失败,",e);
            sender.send(RpcMessage.createResponse(request.getRid(), RpcResponse.error(e.getMessage())));
        }
    }
}
