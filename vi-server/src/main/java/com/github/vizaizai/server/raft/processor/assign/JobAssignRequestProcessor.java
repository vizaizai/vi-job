package com.github.vizaizai.server.raft.processor.assign;

import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.github.vizaizai.server.raft.kv.KVOpClosure;
import com.github.vizaizai.server.raft.proto.JobProto;
import com.github.vizaizai.server.service.apply.JobAssignApplyService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 任务分配处理
 * @author liaochongwei
 * @date 2023/5/18 16:36
 */
@Component
public class JobAssignRequestProcessor implements RpcProcessor<JobProto.AssignRequest> {
    @Resource
    private JobAssignApplyService jobAssignApplyService;
    @Override
    public void handleRequest(RpcContext rpcContext, JobProto.AssignRequest request) {
        long jobId = request.getJobId();
        final KVOpClosure closure = new KVOpClosure(rpcContext);
        jobAssignApplyService.assign(jobId, closure);

    }

    @Override
    public String interest() {
        return JobProto.AssignRequest.class.getName();
    }


}
