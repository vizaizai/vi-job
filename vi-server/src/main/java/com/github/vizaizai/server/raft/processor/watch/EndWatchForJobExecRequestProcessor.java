package com.github.vizaizai.server.raft.processor.watch;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.github.vizaizai.server.raft.proto.JobProto;
import com.github.vizaizai.server.raft.proto.ResponseProto;
import com.github.vizaizai.server.timer.watch.WatchDogRunner;
import com.github.vizaizai.server.timer.watch.WatchInstance;

/**
 * 任务执行结束监听处理
 * @author liaochongwei
 * @date 2023/8/16 16:36
 */
public class EndWatchForJobExecRequestProcessor implements RpcProcessor<JobProto.EndWatchForJobExecRequest> {

    @Override
    public void handleRequest(RpcContext rpcContext, JobProto.EndWatchForJobExecRequest request) {
        WatchDogRunner.getInstance().end(WatchInstance.getWatchId1(request.getJobId()));
        rpcContext.sendResponse(ResponseProto.Response.newBuilder().setSuccess(true).build());
    }

    @Override
    public String interest() {
        return JobProto.EndWatchForJobExecRequest.class.getName();
    }


}
