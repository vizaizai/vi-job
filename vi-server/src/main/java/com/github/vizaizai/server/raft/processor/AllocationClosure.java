package com.github.vizaizai.server.raft.processor;

import com.alipay.sofa.jraft.Closure;
import com.github.vizaizai.server.raft.proto.JobProto.Response;

/**
 * @author liaochongwei
 * @date 2023/6/21 14:49
 */
abstract public class AllocationClosure implements Closure {
    private Response response;
    private AllocationCommand command;

    public AllocationCommand getCommand() {
        return command;
    }

    public void setCommand(AllocationCommand command) {
        this.command = command;
    }

    public Response getResponse() {
        return response;
    }

    public void failure(String errorMsg) {
        this.response = Response.newBuilder()
                .setSuccess(false)
                .setErrorMsg(errorMsg)
                .build();
    }

    public void success(String data) {
        Response.Builder builder = Response.newBuilder().setSuccess(true);
        if (data != null) {
            builder.setData(data);
        }
        this.response = builder.build();
    }
}
