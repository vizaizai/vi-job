package com.github.vizaizai.server.service.apply;

import com.github.vizaizai.server.raft.KVApplyService;
import com.github.vizaizai.server.raft.kv.KVCommand;
import com.github.vizaizai.server.raft.kv.KVOpClosure;
import com.github.vizaizai.server.raft.kv.Op;
import com.github.vizaizai.server.raft.kv.Type;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * String应用业务
 * @author liaochongwei
 * @date 2023/7/3 11:29
 */
@Slf4j
@Component
public class StringApplyService extends KVApplyService {
    /**
     * 设置
     * @param key key
     * @param value 值
     * @param closure 回调
     */
    public void set(String key, Object value, KVOpClosure closure) {
        if (!this.checkNode(closure)) {
            return;
        }
        KVCommand command = new KVCommand();
        command.setType(Type.STRING);
        command.setOp(Op.S_SET);
        command.setKey(key);
        command.setValue(value);
        this.apply(command, closure);
    }


}
