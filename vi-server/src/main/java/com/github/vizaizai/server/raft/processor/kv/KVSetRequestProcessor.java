package com.github.vizaizai.server.raft.processor.kv;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.github.vizaizai.server.raft.kv.KVOpClosure;
import com.github.vizaizai.server.raft.kv.Type;
import com.github.vizaizai.server.raft.proto.KVProto;
import com.github.vizaizai.server.raft.proto.ResponseProto;
import com.github.vizaizai.server.service.apply.SetApplyService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * KVSet处理器
 * @author liaochongwei
 * @date 2023/5/18 16:36
 */
@Component
public class KVSetRequestProcessor implements RpcProcessor<KVProto.SetRequest> {
    @Resource
    private SetApplyService setApplyService;
    @Override
    public void handleRequest(RpcContext rpcContext, KVProto.SetRequest request) {
        final KVOpClosure closure =  new KVOpClosure(rpcContext);
        String op = request.getOp();
        switch (op) {
            case "add":
                setApplyService.add(request.getKey(), request.getElement(), closure);
                break;
            case "remove":
                setApplyService.remove(request.getKey(), request.getElement(), closure);
                break;
            case "rm_key":
                setApplyService.rmKey(request.getKey(), Type.SET, closure);
            default:
                rpcContext.sendResponse(ResponseProto.Response.newBuilder().setSuccess(false).setErrorMsg("op不支持").build());
        }


    }

    @Override
    public String interest() {
        return KVProto.SetRequest.class.getName();
    }


}
