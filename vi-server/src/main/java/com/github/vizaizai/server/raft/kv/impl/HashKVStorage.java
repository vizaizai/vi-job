package com.github.vizaizai.server.raft.kv.impl;

import com.github.vizaizai.server.raft.kv.KVStorage;
import com.github.vizaizai.server.raft.kv.metadata.HashMetadata;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * hash类型接口存储实现
 * @author liaochongwei
 * @date 2023/6/30 14:34
 */
@Slf4j
public class HashKVStorage extends KVStorage {
    public Object hGet(String key, String hashKey) {
        lock.readLock().lock();
        try {
            HashMetadata metadata = (HashMetadata) this.get(key);
            if (metadata != null) {
                return metadata.get(hashKey);
            }
            return null;
        }finally {
            lock.readLock().unlock();
        }

    }

    /**
     * 放入一个元素，并且返回放入的元素
     * @param key 健
     * @param hashKey hash健
     * @param value 值
     * @return Object
     */
    public Object hPut(String key, String hashKey, Object value) {
        lock.writeLock().lock();
        try {
            HashMetadata metadata = (HashMetadata) this.get(key);
            if (metadata == null) {
                metadata = new HashMetadata();
                this.put(key, metadata);
            }
            return metadata.put(hashKey, value);
        }finally {
            lock.writeLock().unlock();
        }
    }

    public Object hRemove(String key, String hashKey) {
        lock.writeLock().lock();
        try {
            HashMetadata metadata = (HashMetadata) this.get(key);
            if (metadata == null) {
               return null;
            }
            Object value = metadata.remove(hashKey);
            if (metadata.getValue().size() == 0) {
                this.remove(key);
            }
            return value;
        }finally {
            lock.writeLock().unlock();
        }
    }

    public Map<String, Object> hEntries(String key) {
        HashMetadata metadata = (HashMetadata) this.get(key);
        if (metadata == null) {
            return null;
        }
        return metadata.getValue();
    }

}
