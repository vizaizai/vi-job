package com.github.vizaizai.server.raft.kv;

import cn.hutool.json.JSONUtil;
import com.alipay.remoting.serialization.SerializerManager;
import com.github.vizaizai.server.raft.kv.impl.HashKVStorage;
import com.github.vizaizai.server.raft.kv.impl.SetKVStorage;
import com.github.vizaizai.server.raft.kv.impl.StringKVStorage;
import com.github.vizaizai.server.raft.kv.metadata.Metadata;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * KV存储
 * @author liaochongwei
 * @date 2023/6/30 10:07
 */
@Slf4j
public abstract class KVStorage {
    /**
     * 读写锁
     */
    public static final ReadWriteLock lock = new ReentrantReadWriteLock();
    /**
     * 元数据map
     */
    private static final Map<String, Metadata> metadataMap = new ConcurrentHashMap<>();
    /**
     * get操作
     * @param key 健
     * @return 值
     */
    protected Metadata get(String key) {
        return metadataMap.get(key);
    }
    /**
     * rm操作
     * @param key 健
     */
    public Metadata remove(String key) {
        return metadataMap.remove(key);
    }

    /**
     * put操作
     * @param key 健
     * @param metadata 元数据
     */
    protected Metadata put(String key, Metadata metadata) {
        return metadataMap.put(key, metadata);
    }
    /**
     * 加载所有数据
     * @return byte[]
     */
    public static byte[] loadData() {
        try {
            return SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(metadataMap);
        }catch (Exception e) {
            log.error("序列化元数据错误，",e);
            throw new RuntimeException("序列化元数据错误");
        }
    }
    /**
     * 初始化数据
     * @param data 源数据
     */
    public static void initData(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        lock.writeLock().lock();
        try {
            Map<String, Metadata> map = SerializerManager.getSerializer(SerializerManager.Hessian2).deserialize(data, Map.class.getName());
            map.forEach(metadataMap::putIfAbsent);
        }catch (Exception e) {
            log.error("初始化元数据失败，",e);
        }finally {
            lock.writeLock().unlock();
        }
    }

    public static  Map<String, Metadata> getMataMap() {
        return metadataMap;
    }

    /**
     * 执行命令
     * @param command 命令参数
     * @return Object
     */
    public static Object execute(KVCommand command) {
        log.info("操作命令: {}", JSONUtil.toJsonStr(command));
        byte type = command.getType();
        switch (type) {
            case Type.STRING:
                return executeForString(command);
            case Type.HASH:
                return executeForHash(command);
            case Type.SET:
                return executeForSet(command);
            default:
                throw new IllegalArgumentException("不支持的数据类型");
        }
    }

    private static Object executeForString(KVCommand command) {
        StringKVStorage storage = new StringKVStorage();
        switch (command.getOp()) {
            case Op.RM:
                return storage.remove(command.getKey());
            case Op.S_SET:
                return storage.sSet(command.getKey(),command.getValue());
            case Op.S_GET:
                return storage.sGet(command.getKey());
            default:
                throw new IllegalArgumentException("string类型不支持的操作");
        }
    }

    private static Object executeForHash(KVCommand command) {
        HashKVStorage storage = new HashKVStorage();
        switch (command.getOp()) {
            case Op.RM:
                return storage.remove(command.getKey());
            case Op.H_PUT:
                return storage.hPut(command.getKey(), command.getHashKey(), command.getValue());
            case Op.H_GET:
                return storage.hGet(command.getKey(), command.getHashKey());
            case Op.H_RM:
                return storage.hRemove(command.getKey(), command.getHashKey());
            case Op.H_ENTRIES:
                return storage.hEntries(command.getKey());
            default:
                throw new IllegalArgumentException("hash类型不支持的操作");
        }
    }

    private static Object executeForSet(KVCommand command) {
        SetKVStorage storage = new SetKVStorage();
        switch (command.getOp()) {
            case Op.RM:
                return storage.remove(command.getKey());
            case Op.ST_ADD:
                return storage.stAdd(command.getKey(), command.getValue());
            case Op.ST_REMOVE:
                return storage.stRemove(command.getKey(), command.getValue());
            case Op.ST_MEMBERS:
                return storage.stMembers(command.getKey());
            default:
                throw new IllegalArgumentException("set类型不支持的操作");
        }
    }


}
