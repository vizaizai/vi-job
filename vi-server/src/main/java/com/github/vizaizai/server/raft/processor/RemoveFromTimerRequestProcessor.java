package com.github.vizaizai.server.raft.processor;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.github.vizaizai.server.raft.proto.JobProto;
import com.github.vizaizai.server.timer.JobTriggerTimer;

/**
 * 从Timer中删除任务请求处理
 * @author liaochongwei
 * @date 2023/5/18 16:36
 */
public class RemoveFromTimerRequestProcessor implements RpcProcessor<JobProto.RemoveFromTimerRequest> {

    @Override
    public void handleRequest(RpcContext rpcContext, JobProto.RemoveFromTimerRequest request) {
        JobTriggerTimer.getInstance().remove(request.getJobId());
        rpcContext.sendResponse(JobProto.Response.newBuilder().setSuccess(true).build());
    }

    @Override
    public String interest() {
        return JobProto.RemoveFromTimerRequest.class.getName();
    }


}
