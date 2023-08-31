package com.github.vizaizai.server.raft.kv.impl;

import com.github.vizaizai.server.raft.kv.KVStorage;
import com.github.vizaizai.server.raft.kv.metadata.Metadata;
import com.github.vizaizai.server.raft.kv.metadata.SetMetadata;
import com.github.vizaizai.server.raft.kv.metadata.StringMetadata;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * set类型接口存储实现
 * @author liaochongwei
 * @date 2023/6/30 14:34
 */
@Slf4j
public class SetKVStorage extends KVStorage {
    /**
     * 读写锁
     */
    public static final ReadWriteLock lock = new ReentrantReadWriteLock();
    @Override
    protected void init(String key, Metadata metadata) {
        lock.writeLock().lock();
        try {
            SetMetadata srcSetMetadata = (SetMetadata) metadata;
            SetMetadata newSetMetadata = (SetMetadata) this.get(key);
            if (newSetMetadata == null) {
                this.put(key, srcSetMetadata);
                return;
            }
            srcSetMetadata.foreach(newSetMetadata::add);
        }finally {
            lock.writeLock().unlock();
        }
    }
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
            if (metadata.size() == 0) {
                this.remove(key);
            }
            return re;
        }finally {
            lock.writeLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public Set<Object> stMembers(String key) {
        SetMetadata metadata = (SetMetadata) this.get(key);
        if (metadata == null) {
            return null;
        }
        return (Set<Object>) metadata.getData();
    }
}
