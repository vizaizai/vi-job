package com.github.vizaizai.server.raft.kv.metadata;

/**
 * String类型元数据
 * @author liaochongwei
 * @date 2023/6/30 14:47
 */
public class StringMetadata implements Metadata {
    /**
     * 值
     */
    private final Object data;

    public StringMetadata(Object value) {
        this.data = value;
    }


    @Override
    public Object getData() {
        return this.data;
    }

    @Override
    public String getType() {
        return "string";
    }
}
