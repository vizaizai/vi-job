package com.github.vizaizai.server.raft;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.NodeImpl;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.error.RaftError;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.raft.kv.KVCommand;
import com.github.vizaizai.server.raft.kv.KVOpClosure;
import com.github.vizaizai.server.raft.kv.Op;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.ByteBuffer;

/**
 * @author liaochongwei
 * @date 2023/7/7 11:14
 */
@Component
@Slf4j
public class KVApplyService {
    @Resource
    protected RaftServer raftServer;

    /**
     * 检查JRaft状态
     * @param closure 回调
     * @return boolean
     */
    protected boolean checkNode(KVOpClosure closure) {
        NodeImpl node = (NodeImpl) raftServer.getNode();
        if (node == null) {
            closure.setResult(Result.handleFailure("JRaft服务未启动"));
            closure.run(new Status(RaftError.ESHUTDOWN, closure.getResult().getMsg()));
            return false;
        }
        if (!node.isLeader()) {
            closure.setResult(Result.handleFailure("Not leader"));
            closure.run(new Status(RaftError.EPERM, closure.getResult().getMsg()));
            return false;
        }
        return true;
    }

    /**
     * 删除
     * @param key key
     * @param closure 回调
     */
    public void rmKey(String key, byte type, KVOpClosure closure) {
        if (!this.checkNode(closure)) {
            return;
        }
        KVCommand command = new KVCommand();
        command.setType(type);
        command.setOp(Op.RM);
        command.setKey(key);
        this.apply(command, closure);
    }

    protected void apply(KVCommand command, KVOpClosure closure) {
        // apply到所有节点上
        try {
            final Task task = new Task();
            task.setData(ByteBuffer.wrap(SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(command)));
            closure.setCommand(command);
            task.setDone(closure);
            this.raftServer.getNode().apply(task);
        }catch (CodecException e) {
            closure.setResult(Result.handleFailure(e.getMessage()));
            closure.run(new Status(RaftError.EINTERNAL, e.getMessage()));
        }
    }
}
