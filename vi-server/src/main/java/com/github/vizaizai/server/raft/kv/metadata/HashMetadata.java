package com.github.vizaizai.server.raft.kv.metadata;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hash类型元数据
 * @author liaochongwei
 * @date 2023/6/30 14:47
 */
@Data
public class HashMetadata implements Metadata{
    /**
     * 类型
     */
    private final String type = "hash";
    /**
     * 值
     */
    private Map<String, Object> value;

    public HashMetadata() {
        this.value = new ConcurrentHashMap<>();
    }

    public Object put(String key, Object value) {
        this.value.put(key, value);
        return value;
    }

    public Object remove(String key) {
        return this.value.remove(key);
    }

    public Object get(String key) {
        return this.value.get(key);
    }

    @Override
    public Object getData() {
        return this.value;
    }
}
