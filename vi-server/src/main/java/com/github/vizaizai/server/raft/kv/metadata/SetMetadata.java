package com.github.vizaizai.server.raft.kv.metadata;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * set类型元数据
 * @author liaochongwei
 * @date 2023/6/30 14:47
 */
@Data
public class SetMetadata implements Metadata {
    /**
     * 类型
     */
    private final String type = "set";
    /**
     * 值
     */
    private Set<Object> value;

    public SetMetadata() {
        this.value = new HashSet<>();
    }

    public Object add(Object e) {
        return this.value.add(e);
    }

    public Object remove(Object e) {
        return this.value.remove(e);
    }

    @Override
    public Object getData() {
        return this.value;
    }
}
