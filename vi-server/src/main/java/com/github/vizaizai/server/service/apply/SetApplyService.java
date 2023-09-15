package com.github.vizaizai.server.service.apply;

import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.raft.KVApplyService;
import com.github.vizaizai.server.raft.kv.KVCommand;
import com.github.vizaizai.server.raft.kv.KVOpClosure;
import com.github.vizaizai.server.raft.kv.Op;
import com.github.vizaizai.server.raft.kv.Type;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Set应用业务
 * @author liaochongwei
 * @date 2023/7/3 11:29
 */
@Slf4j
@Component
public class SetApplyService extends KVApplyService {
    /**
     * 添加
     * @param key key
     * @param element 元素
     * @param closure 回调
     */
    public void add(String key, Object element, KVOpClosure closure) {
        if (!this.checkNode(closure)) {
            return;
        }
        KVCommand command = new KVCommand();
        command.setType(Type.SET);
        command.setOp(Op.ST_ADD);
        command.setKey(key);
        command.setValue(element);
        this.apply(command, closure);
    }

    /**
     * 移除
     * @param key key
     * @param element 元素
     * @param closure 回调
     */
    public void remove(String key, Object element, KVOpClosure closure) {
        if (!this.checkNode(closure)) {
            return;
        }
        KVCommand command = new KVCommand();
        command.setType(Type.SET);
        command.setOp(Op.ST_REMOVE);
        command.setKey(key);
        command.setValue(element);
        this.apply(command, closure);
    }
}
