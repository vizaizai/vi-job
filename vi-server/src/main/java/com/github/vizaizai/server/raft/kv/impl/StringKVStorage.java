package com.github.vizaizai.server.raft.kv.impl;

import com.github.vizaizai.server.raft.kv.KVStorage;
import com.github.vizaizai.server.raft.kv.metadata.StringMetadata;
import lombok.extern.slf4j.Slf4j;

/**
 * string类型接口存储实现
 * @author liaochongwei
 * @date 2023/6/30 14:34
 */
@Slf4j
public class StringKVStorage extends KVStorage {

    public Object sGet(String key) {
        StringMetadata metadata = (StringMetadata) this.get(key);
        if (metadata == null) {
            return null;
        }
        return metadata.getValue();
    }

    public Object sSet(String key, Object value) {
        return this.put(key, new StringMetadata(value)).getData();
    }
}
