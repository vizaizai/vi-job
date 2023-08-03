package com.github.vizaizai.server.raft.processor.timer;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.raft.proto.JobProto;
import com.github.vizaizai.server.raft.proto.ResponseProto;
import com.github.vizaizai.server.timer.JobTriggerTimer;

/**
 * 将任务推入Timer请求处理
 * @author liaochongwei
 * @date 2023/5/18 16:36
 */
public class PushIntoTimerRequestProcessor implements RpcProcessor<JobProto.PushIntoTimerRequest> {
    @Override
    public void handleRequest(RpcContext rpcContext, JobProto.PushIntoTimerRequest request) {
        Job job = new Job();
        job.setId(request.getJobId());
        job.setName(request.getName());
        job.setWorkerId(request.getWorkerId());
        if (request.getStartTime() > 0L) {
            job.setStartTime(LocalDateTimeUtil.of(request.getStartTime()));
        }
        if (request.getEndTime() > 0L) {
            job.setEndTime(LocalDateTimeUtil.of(request.getEndTime()));
        }
        job.setProcessorType(request.getProcessorType());
        job.setProcessor(request.getProcessor());
        if (Utils.isNotBlank(request.getParam())) {
            job.setParam(request.getParam());
        }
        job.setTriggerType(request.getTriggerType());
        job.setCron(request.getCron());
        job.setSpeedS(request.getSpeedS());
        job.setDelayedS(request.getDelayedS());
        job.setRouteType(request.getRouteType());
        job.setRetryCount(request.getRetryCount());
        job.setTimeoutS(request.getTimeoutS());
        job.setMaxWaitNum(request.getMaxWaitNum());
        job.setLogAutoDelHours(request.getLogAutoDelHours());
        if (request.getNextTriggerTime() > 0L) {
            job.setNextTriggerTime(request.getNextTriggerTime());
        }
        if (request.getLastExecuteEndTime() > 0L) {
            job.setLastExecuteEndTime(request.getLastExecuteEndTime());
        }
        JobTriggerTimer.getInstance().push(job);
        rpcContext.sendResponse(ResponseProto.Response.newBuilder().setSuccess(true).build());
    }

    @Override
    public String interest() {
        return JobProto.PushIntoTimerRequest.class.getName();
    }


}
