package com.github.vizaizai.server.raft.kv.metadata;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * set类型元数据
 * @author liaochongwei
 * @date 2023/6/30 14:47
 */
public class SetMetadata implements Metadata {
    /**
     * 值
     */
    private final Set<Object> data;

    public SetMetadata() {
        this.data = new HashSet<>();
    }

    public Object add(Object e) {
        return this.data.add(e);
    }

    public Object remove(Object e) {
        return this.data.remove(e);
    }

    public void foreach(Consumer<Object> action) {
        data.forEach(action);

    }

    public int size() {
        return data.size();
    }

    @Override
    public Object getData() {
        return this.data;
    }

    @Override
    public String getType() {
        return "set";
    }
}
