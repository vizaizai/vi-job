package com.github.vizaizai.server.raft.kv;

/**
 * 数据类型
 * @author liaochongwei
 * @date 2023/6/30 17:07
 */
public class Op {
    public static final byte RM = 0x01;

    public static final byte S_GET = 0x02;
    public static final byte S_SET = 0x03;

    public static final byte H_GET = 0x04;
    public static final byte H_PUT = 0x05;
    public static final byte H_RM = 0x06;
    public static final byte H_ENTRIES = 0x07;

    public static final byte ST_ADD = 0x08;
    public static final byte ST_REMOVE = 0x09;
    public static final byte ST_MEMBERS = 0x0a;




}
