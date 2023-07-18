package com.github.vizaizai.server.raft.kv.metadata;

import lombok.Data;

/**
 * String类型元数据
 * @author liaochongwei
 * @date 2023/6/30 14:47
 */
@Data
public class StringMetadata implements Metadata {
    /**
     * 类型
     */
    private final String type = "string";
    /**
     * 值
     */
    private Object value;

    public StringMetadata(Object value) {
        this.value = value;
    }


    @Override
    public Object getData() {
        return this.value;
    }
}
