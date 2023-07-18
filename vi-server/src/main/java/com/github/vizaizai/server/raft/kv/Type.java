package com.github.vizaizai.server.raft.kv;

/**
 * 数据类型
 * @author liaochongwei
 * @date 2023/6/30 17:07
 */
public class Type {
    public static final byte STRING = 0x01;
    public static final byte HASH = 0x02;
    public static final byte SET = 0x03;
}
