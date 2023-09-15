package com.github.vizaizai.server.service.apply;

import cn.hutool.core.collection.CollUtil;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.NodeImpl;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.error.RaftError;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.entity.JobGroup;
import com.github.vizaizai.server.raft.KVApplyService;
import com.github.vizaizai.server.raft.kv.KVCommand;
import com.github.vizaizai.server.raft.kv.KVOpClosure;
import com.github.vizaizai.server.raft.kv.Op;
import com.github.vizaizai.server.raft.kv.Type;
import com.github.vizaizai.server.raft.kv.impl.HashKVStorage;
import com.github.vizaizai.server.service.GlobalJobGroupManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 任务分配应用服务
 * @author liaochongwei
 * @date 2023/7/3 11:29
 */
@Slf4j
@Component
public class JobAssignApplyService extends KVApplyService {
    @Resource
    private GlobalJobGroupManager globalJobGroupManager;
    /**
     * 分配任务
     * @param jobId 任务id
     * @param closure 回调
     */
    public void assign(Long jobId, KVOpClosure closure) {
        NodeImpl node = (NodeImpl) raftServer.getNode();
        if (!this.checkNode(closure)) {
            return;
        }
        HashKVStorage storage = new HashKVStorage();
        // 获取在线调度节点
        List<PeerId> peerIds = node.listAlivePeers();
        if (CollUtil.isEmpty(peerIds)) {
            closure.setResult(Result.handleFailure("集群活跃节点为空"));
            closure.run(new Status(RaftError.ENOENT, closure.getResult().getMsg()));
            return;
        }

        String jobKey = String.valueOf(jobId);
        final Map<String, JobGroup> jobGroups = new HashMap<>();
        Map<String, Object> jobAssignInfos = storage.hEntries(Commons.JOB_ASSIGN_KEY);
        if (Utils.isNotEmpty(jobAssignInfos)) {
            if (jobAssignInfos.containsKey(jobKey)) {
                closure.setResult(Result.handleSuccess(jobAssignInfos.get(jobKey)));
                closure.run(Status.OK());
                return;
            }

            // 初始化任务组
            jobAssignInfos.forEach((k, v) -> {
                String tAddress = v.toString();
                Long tJobId = Long.valueOf(k);
                JobGroup jobGroup = jobGroups.get(tAddress);
                if (jobGroup != null) {
                    jobGroup.putJob(tJobId);
                }else {
                    jobGroups.put(tAddress, new JobGroup(tAddress, tJobId));
                }
            });
        }

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
        KVCommand command = new KVCommand();
        command.setType(Type.HASH);
        command.setOp(Op.H_PUT);
        command.setKey(Commons.JOB_ASSIGN_KEY);
        command.setHashKey(jobKey);
        command.setValue(selectNode);

        this.apply(command, closure);
    }

    /**
     * 移除任务
     * @param jobId 任务id
     * @param closure 回调
     */
    public void remove(Long jobId, KVOpClosure closure) {
        if (!this.checkNode(closure)) {
            return;
        }
        KVCommand command = new KVCommand();
        command.setType(Type.HASH);
        command.setOp(Op.H_RM);
        command.setKey(Commons.JOB_ASSIGN_KEY);
        command.setHashKey(String.valueOf(jobId));

        this.apply(command, closure);
    }

    /**
     * 获取任务分配信息
     * @param jobId 任务id
     * @param initIfAbsent 缺席是否初始化
     * @return 节点地址
     */
    public String get(Long jobId, boolean initIfAbsent) {
        HashKVStorage storage = new HashKVStorage();
        Object address = storage.hGet(Commons.JOB_ASSIGN_KEY, String.valueOf(jobId));
        if (address != null) {
            return address.toString();
        }
        if (initIfAbsent) {
            return globalJobGroupManager.assign(jobId);
        }
        return null;
    }
}
