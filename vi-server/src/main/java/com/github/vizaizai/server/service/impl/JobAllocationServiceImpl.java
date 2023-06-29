package com.github.vizaizai.server.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.NodeImpl;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.error.RaftError;
import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.entity.JobGroup;
import com.github.vizaizai.server.raft.RaftServer;
import com.github.vizaizai.server.raft.SnapshotFile;
import com.github.vizaizai.server.raft.processor.AllocationClosure;
import com.github.vizaizai.server.raft.processor.AllocationCommand;
import com.github.vizaizai.server.service.JobAllocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author liaochongwei
 * @date 2023/6/19 17:22
 */
@Slf4j
@Service
public class JobAllocationServiceImpl implements JobAllocationService {
    /**
     * （任务，节点地址）映射
     */
    private static final Map<Long, String> jobMaps = new ConcurrentHashMap<>();
    @Resource
    private RaftServer raftServer;
    /**
     * 读写锁
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void put(Long jobId, AllocationClosure closure) {
        NodeImpl node = (NodeImpl) raftServer.getNode();
        if (!this.checkNode(closure)) {
            return;
        }
        String errorMsg;
        // 获取在线调度节点
        List<PeerId> peerIds = node.listAlivePeers();
        if (CollUtil.isEmpty(peerIds)) {
            errorMsg = "集群活跃节点为空";
            closure.failure(errorMsg);
            closure.run(new Status(RaftError.ENOENT,errorMsg));
            return;
        }

        lock.readLock().lock();
        try {
            if (jobMaps.containsKey(jobId)) {
                closure.success(jobMaps.get(jobId));
                closure.run(Status.OK());
                return;
            }
            final Map<String, JobGroup> jobGroups = new HashMap<>();
            log.info(">>>>>>>>>put:{}", JSONUtil.toJsonStr(jobMaps));
            // 初始化任务组
            jobMaps.forEach((k, v) -> {
                JobGroup jobGroup = jobGroups.get(v);
                if (jobGroups.get(v) != null) {
                    jobGroup.putJob(k);
                }else {
                    jobGroups.put(v, new JobGroup(v, k));
                }
            });
            String selectNode = null;
            // 分配规则, 按数量分配,优先分配到任务少的节点上
            List<JobGroup> aliveGroups = new ArrayList<>();
            for (PeerId peerId : peerIds) {
                String address = peerId.toString();
                JobGroup nodeDispatchDetail = jobGroups.get(address);
                if (nodeDispatchDetail == null) {
                    selectNode = address;
                    break;
                }else {
                    aliveGroups.add(nodeDispatchDetail);
                }
            }
            if (selectNode == null) {
                // 按任务数量由小到打排序
                aliveGroups.sort(Comparator.comparing(e->e.getJobSet().size()));
                selectNode = aliveGroups.get(0).getAddress();
            }
            this.apply(AllocationCommand.createPut(jobId, selectNode), closure);
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void rm(Long jobId, AllocationClosure closure) {
        if (!this.checkNode(closure)) {
            return;
        }
        this.apply(AllocationCommand.createRm(jobId), closure);
    }

    @Override
    public void doPut(Long jobId, String address) {
        lock.writeLock().lock();
        try {
            jobMaps.put(jobId, address);
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void doRm(Long jobId) {
        lock.writeLock().lock();
        try {
            jobMaps.remove(jobId);
        }finally {
            lock.writeLock().unlock();
        }

    }

    @Override
    public String get(Long jobId) {
        lock.readLock().lock();
        try {
            String address = jobMaps.get(jobId);
            if (address == null) {
                String snapshotUri = raftServer.getNode().getOptions().getSnapshotUri();
                File file = new File(snapshotUri + File.separator + Commons.SNAPSHOT_NAME);
                // 存在快照文件
                if (file.exists()) {
                    SnapshotFile snapshot = new SnapshotFile(file.getPath());
                    byte[] bytes = snapshot.load();
                    if (bytes == null || bytes.length == 0) {
                        return null;
                    }
                    Map<Long, String> snapshotMap = SerializerManager.getSerializer(SerializerManager.Hessian2).deserialize(bytes, Map.class.getName());
                    return snapshotMap.get(jobId);
                }
            }
            log.info(">>>>>>>>>>>>>>>>>>>jobMaps:{}", JSONUtil.toJsonStr(jobMaps));
            return address;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.readLock().unlock();
        }

    }

    @Override
    public byte[] getData() {
        lock.readLock().lock();
        try {
            return SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(jobMaps);
        }catch (Exception e) {
            log.error("获取任务映射数据失败，",e);
            throw new RuntimeException("序列化任务映射错误");
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void init(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        lock.writeLock().lock();
        try {
            Map<Long, String> map = SerializerManager.getSerializer(SerializerManager.Hessian2).deserialize(data, Map.class.getName());
            map.forEach(jobMaps::putIfAbsent);
        }catch (Exception e) {
            log.error("初始化任务映射失败，",e);
        }finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 检查JRaft状态
     * @param closure 回调
     * @return boolean
     */
    private boolean checkNode(AllocationClosure closure) {
        NodeImpl node = (NodeImpl) raftServer.getNode();
        String errorMsg;
        if (node == null) {
            errorMsg = "JRaft服务未启动";
            closure.failure(errorMsg);
            closure.run(new Status(RaftError.ESHUTDOWN,errorMsg));
            return false;
        }
        if (!node.isLeader()) {
            errorMsg = "Not leader";
            closure.failure(errorMsg);
            closure.run(new Status(RaftError.EPERM, errorMsg));
            return false;
        }
        return true;
    }

    private void apply(AllocationCommand command, AllocationClosure closure) {
        // apply到所有节点上
        try {
            final Task task = new Task();
            task.setData(ByteBuffer.wrap(SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(command)));
            closure.setCommand(command);
            task.setDone(closure);
            this.raftServer.getNode().apply(task);
        }catch (CodecException e) {
            closure.failure(e.getMessage());
            closure.run(new Status(RaftError.EINTERNAL, e.getMessage()));
        }
    }
}
