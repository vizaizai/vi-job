package com.github.vizaizai.server.raft.kv;

import com.alipay.sofa.jraft.Closure;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.raft.proto.ResponseProto.Response;

/**
 * kv操作回调
 * @author liaochongwei
 * @date 2023/6/21 14:49
 */
abstract public class KVOpClosure implements Closure {

    private KVCommand command;

    private Result<Object> result;

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
}
