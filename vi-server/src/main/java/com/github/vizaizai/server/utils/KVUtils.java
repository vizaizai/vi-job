package com.github.vizaizai.server.utils;

import com.alipay.sofa.jraft.entity.PeerId;
import com.github.vizaizai.server.raft.RaftServer;
import com.github.vizaizai.server.raft.kv.KVStorage;
import com.github.vizaizai.server.raft.kv.Type;
import com.github.vizaizai.server.raft.kv.impl.SetKVStorage;
import com.github.vizaizai.server.raft.kv.impl.StringKVStorage;
import com.github.vizaizai.server.raft.kv.metadata.Metadata;
import com.github.vizaizai.server.raft.proto.KVProto;
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
     * set-add
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
        KVProto.SetRequest request = KVProto.SetRequest
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
     * set-remove
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
        KVProto.SetRequest request = KVProto.SetRequest
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
     * set-members
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

    /**
     * string-get
     * @param key key
     * @return String
     */
    public static String get(String key) {
        StringKVStorage storage = new StringKVStorage();
        Object object = storage.sGet(key);
        if (object == null) {
            return null;
        }
        return object.toString();
    }

    /**
     * string-set
     * @param key key
     */
    public static void set(String key, String value) {
        if (!getRaftServer().isCluster()) {
            StringKVStorage storage = new StringKVStorage();
            storage.sSet(key,value);
            return;
        }
        PeerId leaderId = getRaftServer().getLeader();
        KVProto.StringRequest request = KVProto.StringRequest
                .newBuilder()
                .setOp("set")
                .setKey(key)
                .setValue(value)
                .build();
        try {
            ResponseProto.Response response = (ResponseProto.Response) raftServer
                    .getRpcClient().invokeSync(leaderId.getEndpoint(), request, 3000);
            if (!response.getSuccess()) {
                throw new RuntimeException("s-set error." + response.getErrorMsg());
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 删除key
     * @param key key
     */
    public static void removeKey(String key, byte type) {
        if (!getRaftServer().isCluster()) {
            KVStorage storage = new KVStorage() {
                @Override
                protected Metadata get(String key) {
                    return super.get(key);
                }
            };
            storage.remove(key);
            return;
        }
        PeerId leaderId = getRaftServer().getLeader();
        Object request;
        switch (type) {
            case Type.SET:
                request = KVProto.SetRequest
                        .newBuilder()
                        .setOp("rm_key")
                        .setKey(key)
                        .build();
                break;
            case Type.STRING:
                request = KVProto.StringRequest
                        .newBuilder()
                        .setOp("rm_key")
                        .setKey(key)
                        .build();
                break;
            default:
                return;
        }
        try {
            ResponseProto.Response response = (ResponseProto.Response) raftServer
                    .getRpcClient().invokeSync(leaderId.getEndpoint(), request, 3000);
            if (!response.getSuccess()) {
                throw new RuntimeException("remove-key error." + response.getErrorMsg());
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }







    private static RaftServer getRaftServer() {
        if (raftServer == null) {
            raftServer = ContextUtil.getBean(RaftServer.class);
        }
        return raftServer;
    }
}
