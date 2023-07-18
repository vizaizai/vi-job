package com.github.vizaizai.server.raft.kv;

import lombok.Data;

import java.io.Serializable;

/**
 * KV命令
 * @author liaochongwei
 * @date 2023/5/18 14:18
 */
@Data
public class KVCommand implements Serializable {
    /**
     * 数据类型
     */
    private byte type;
    /**
     * 操作符
     */
    private byte op;
    /**
     * key
     */
    private String key;
    /**
     * value
     */
    private Object value;
    /**
     * hashKey
     */
    private String hashKey;

}
