package com.github.vizaizai.server.raft.kv;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.raft.proto.ResponseProto.Response;

/**
 * kv操作回调
 * @author liaochongwei
 * @date 2023/6/21 14:49
 */
public class KVOpClosure implements Closure {
    private final RpcContext rpcContext;
    private KVCommand command;

    private Result<Object> result;

    public KVOpClosure(RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    public KVCommand getCommand() {
        return command;
    }

    public void setCommand(KVCommand command) {
        this.command = command;
    }

    public Result<Object> getResult() {
        return result;
    }

    public void setResult(Result<Object> result) {
        this.result = result;
    }

    public Response getResponse() {
        Response.Builder builder = Response.newBuilder();
        builder.setSuccess(this.result.isSuccess());
        if (this.result.getMsg() != null) {
            builder.setErrorMsg(this.result.getMsg());
        }
        if (this.result.getData()!= null
                && this.result.getData() instanceof String) {
            builder.setData((String) this.result.getData());
        }
        return builder.build();
    }

    @Override
    public void run(Status status) {
        rpcContext.sendResponse(this.getResponse());
    }
}
