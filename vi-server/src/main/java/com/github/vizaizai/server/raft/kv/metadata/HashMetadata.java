package com.github.vizaizai.server.raft.kv.metadata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Hash类型元数据
 * @author liaochongwei
 * @date 2023/6/30 14:47
 */
public class HashMetadata implements Metadata{
    /**
     * 值
     */
    private final Map<String, Object> data;

    public HashMetadata() {
        this.data = new ConcurrentHashMap<>();
    }

    public Object put(String key, Object value) {
        this.data.put(key, value);
        return value;
    }

    public void putIfAbsent(String key, Object value) {
        this.data.putIfAbsent(key, value);
    }


    public Object remove(String key) {
        return this.data.remove(key);
    }

    public Object get(String key) {
        return this.data.get(key);
    }

    public int size() {
        return this.data.size();
    }

    public void foreach(BiConsumer<String, Object> action) {
        data.forEach(action);
    }

    @Override
    public String getType() {
        return "hash";
    }

    @Override
    public Object getData() {
        return this.data;
    }
}
