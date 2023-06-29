package com.github.vizaizai.server.raft.processor;

import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.github.vizaizai.server.raft.proto.JobProto;
import com.github.vizaizai.server.service.JobAllocationService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Rm请求处理
 * @author liaochongwei
 * @date 2023/5/18 16:36
 */
@Component
public class JobRmRequestProcessor implements RpcProcessor<JobProto.RmRequest> {
    @Resource
    private JobAllocationService jobAllocationService;
    @Override
    public void handleRequest(RpcContext rpcContext, JobProto.RmRequest rmRequest) {
        long jobId = rmRequest.getJobId();
        final AllocationClosure closure = new AllocationClosure() {
            @Override
            public void run(Status status) {
                rpcContext.sendResponse(getResponse());
            }
        };
        jobAllocationService.rm(jobId, closure);
    }

    @Override
    public String interest() {
        return JobProto.RmRequest.class.getName();
    }


}
