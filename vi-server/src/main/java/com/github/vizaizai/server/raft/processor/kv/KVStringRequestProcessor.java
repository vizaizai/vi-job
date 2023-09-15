package com.github.vizaizai.server.raft.processor.kv;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.github.vizaizai.server.raft.kv.KVOpClosure;
import com.github.vizaizai.server.raft.kv.Type;
import com.github.vizaizai.server.raft.proto.KVProto;
import com.github.vizaizai.server.raft.proto.ResponseProto;
import com.github.vizaizai.server.service.apply.StringApplyService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * KVString处理器
 * @author liaochongwei
 * @date 2023/5/18 16:36
 */
@Component
public class KVStringRequestProcessor implements RpcProcessor<KVProto.StringRequest> {
    @Resource
    private StringApplyService stringApplyService;
    @Override
    public void handleRequest(RpcContext rpcContext, KVProto.StringRequest request) {
        final KVOpClosure closure = new KVOpClosure(rpcContext);
        String op = request.getOp();
        switch (op) {
            case "set":
                stringApplyService.set(request.getKey(), request.getValue(), closure);
                break;
            case "rm_key":
                stringApplyService.rmKey(request.getKey(), Type.STRING, closure);
                break;
            default:
                rpcContext.sendResponse(ResponseProto.Response.newBuilder().setSuccess(false).setErrorMsg("op不支持").build());
        }
    }

    @Override
    public String interest() {
        return KVProto.StringRequest.class.getName();
    }


}
