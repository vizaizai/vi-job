package com.github.vizaizai.server.utils;

import com.alipay.sofa.jraft.entity.PeerId;
import com.github.vizaizai.server.raft.RaftServer;
import com.github.vizaizai.server.raft.kv.impl.SetKVStorage;
import com.github.vizaizai.server.raft.proto.KVSetProto;
import com.github.vizaizai.server.raft.proto.ResponseProto;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * kv工具
 * @author liaochongwei
 * @date 2023/7/10 10:12
 */
public class KVUtils {

    private static RaftServer raftServer = null;
    private KVUtils() {
    }


    /**
     * set-添加元素
     * @param key key
     * @param el 集合元素
     */
    public static void stAdd(String key, String el) {
        if (!getRaftServer().isCluster()) {
            SetKVStorage storage = new SetKVStorage();
            storage.stAdd(key, el);
            return;
        }
        PeerId leaderId = getRaftServer().getLeader();
        KVSetProto.Request request = KVSetProto.Request
                .newBuilder()
                .setOp("add")
                .setKey(key)
                .setElement(el)
                .build();
        try {
            ResponseProto.Response response = (ResponseProto.Response) raftServer
                    .getRpcClient().invokeSync(leaderId.getEndpoint(), request, 3000);
            if (!response.getSuccess()) {
                throw new RuntimeException("st-add error." + response.getErrorMsg());
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    /**
     * set-移除元素
     * @param key key
     * @param el 集合元素
     */
    public static void stRemove(String key, String el) {
        if (!getRaftServer().isCluster()) {
            SetKVStorage storage = new SetKVStorage();
            storage.stRemove(key, el);
            return;
        }
        PeerId leaderId = getRaftServer().getLeader();
        KVSetProto.Request request = KVSetProto.Request
                .newBuilder()
                .setOp("remove")
                .setKey(key)
                .setElement(el)
                .build();
        try {
            ResponseProto.Response response = (ResponseProto.Response) raftServer
                    .getRpcClient().invokeSync(leaderId.getEndpoint(), request, 3000);
            if (!response.getSuccess()) {
                throw new RuntimeException("st-remove error." + response.getErrorMsg());
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取集合元素
     * @param key key
     * @return Set
     */
    public static Set<String> stMembers(String key) {
        SetKVStorage storage = new SetKVStorage();
        Set<Object> objects = storage.stMembers(key);
        if (objects == null) {
            return Collections.emptySet();
        }
        return objects.stream().map(Object::toString).collect(Collectors.toSet());
    }


    private static RaftServer getRaftServer() {
        if (raftServer == null) {
            raftServer = ContextUtil.getBean(RaftServer.class);
        }
        return raftServer;
    }
}
