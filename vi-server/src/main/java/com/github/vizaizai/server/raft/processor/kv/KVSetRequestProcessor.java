package com.github.vizaizai.server.raft.processor.kv;

import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.github.vizaizai.server.raft.kv.KVOpClosure;
import com.github.vizaizai.server.raft.proto.KVSetProto;
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
public class KVSetRequestProcessor implements RpcProcessor<KVSetProto.Request> {
    @Resource
    private SetApplyService setApplyService;
    @Override
    public void handleRequest(RpcContext rpcContext, KVSetProto.Request request) {
        final KVOpClosure closure = new KVOpClosure() {
            @Override
            public void run(Status status) {
                rpcContext.sendResponse(getResponse());
            }
        };
        String op = request.getOp();
        switch (op) {
            case "add":
                setApplyService.add(request.getKey(), request.getElement(), closure);
                break;
            case "remove":
                setApplyService.remove(request.getKey(), request.getElement(), closure);
                break;
            default:
                rpcContext.sendResponse(ResponseProto.Response.newBuilder().setSuccess(false).setErrorMsg("op不支持").build());
        }


    }

    @Override
    public String interest() {
        return KVSetProto.Request.class.getName();
    }


}
