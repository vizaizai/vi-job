package com.github.vizaizai.server.raft.kv.metadata;

import java.io.Serializable;

/**
 * 元数据
 * @author liaochongwei
 * @date 2023/6/30 14:51
 */
public interface Metadata extends Serializable {
    String getType();
    Object getData();
}
