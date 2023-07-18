package com.github.vizaizai.server.raft.kv.impl;

import com.github.vizaizai.server.raft.kv.KVStorage;
import com.github.vizaizai.server.raft.kv.metadata.HashMetadata;
import com.github.vizaizai.server.raft.kv.metadata.SetMetadata;
import com.github.vizaizai.server.raft.kv.metadata.StringMetadata;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

/**
 * string类型接口存储实现
 * @author liaochongwei
 * @date 2023/6/30 14:34
 */
@Slf4j
public class SetKVStorage extends KVStorage {

    public Object stAdd(String key, Object e) {
        lock.writeLock().lock();
        try {
            SetMetadata metadata = (SetMetadata) this.get(key);
            if (metadata == null) {
                metadata = new SetMetadata();
                this.put(key, metadata);
            }
            return metadata.add(e);
        }finally {
            lock.writeLock().unlock();
        }
    }

    public Object stRemove(String key, Object e) {
        lock.writeLock().lock();
        try {
            SetMetadata metadata = (SetMetadata) this.get(key);
            if (metadata == null) {
                return null;
            }
            Object re = metadata.remove(e);
            if (metadata.getValue().size() == 0) {
                this.remove(key);
            }
            return re;
        }finally {
            lock.writeLock().unlock();
        }
    }

    public Set<Object> stMembers(String key) {
        SetMetadata metadata = (SetMetadata) this.get(key);
        if (metadata == null) {
            return null;
        }
        return metadata.getValue();
    }
}
