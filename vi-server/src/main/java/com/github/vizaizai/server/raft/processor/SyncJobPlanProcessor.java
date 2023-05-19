package com.github.vizaizai.server.raft.processor;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;

import java.util.concurrent.Executor;

/**
 * @author liaochongwei
 * @date 2023/5/18 16:36
 */
public class SyncJobPlanProcessor implements RpcProcessor<JobProtos.JobPlanPutRequest> {
    @Override
    public void handleRequest(RpcContext rpcContext, JobProtos.JobPlanPutRequest jobPlanPutRequest) {

    }

    @Override
    public String interest() {
        return JobProtos.JobPlanPutRequest.class.getName();
    }

    @Override
    public Executor executor() {
        return RpcProcessor.super.executor();
    }
}
