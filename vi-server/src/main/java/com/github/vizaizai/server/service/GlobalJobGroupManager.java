package com.github.vizaizai.server.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.entity.PeerId;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.raft.RaftServer;
import com.github.vizaizai.server.raft.proto.JobProto;
import com.github.vizaizai.server.timer.JobTriggerTimer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 全局任务分组管理
 * @author liaochongwei
 * @date 2023/6/20 14:29
 */
@Component
@Slf4j
public class GlobalJobGroupManager {
    @Resource
    private RaftServer raftServer;
    @Resource
    private JobAllocationService jobAllocationService;
    private final JobTriggerTimer triggerTimer = JobTriggerTimer.getInstance();
    /**
     * 节点选择
     * @param jobId 任务id
     */
    public void elect(Long jobId) {
        // 单机模式无需分组
        if (!raftServer.isCluster()) {
            return;
        }
        PeerId leaderId = this.getLeader();
        JobProto.PutRequest request = JobProto.PutRequest.newBuilder().setJobId(jobId).build();
        try {
            JobProto.Response response = (JobProto.Response) raftServer.getRpcClient()
                    .invokeSync(leaderId.getEndpoint(), request, 3000);
            if (!response.getSuccess()) {
                throw new RuntimeException("任务节点选择失败");
            }
        }catch (Exception e) {
            log.error("节点选择错误: {}",e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }



    /**
     * 移除分组
     * @param jobId 任务id
     */
    public void remove(Long jobId) {
        if (!raftServer.isCluster()) {
            return;
        }
        PeerId leaderId = this.getLeader();
        JobProto.RmRequest request = JobProto.RmRequest.newBuilder().setJobId(jobId).build();
        try {
            JobProto.Response response = (JobProto.Response) raftServer
                    .getRpcClient().invokeSync(leaderId.getEndpoint(), request, 3000);
            if (!response.getSuccess()) {
                throw new RuntimeException("任务摘除失败");
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 将任务推入Timer
     * @param job
     */
    public void pushIntoTimer(Job job) {
        if (!raftServer.isCluster()) {
            triggerTimer.push(job);
            return;
        }

        String nodeAddress = jobAllocationService.get(job.getId());
        if (Utils.isBlank(nodeAddress)) {
            throw new RuntimeException("任务未分配，请稍等");
        }
        // 执行调度地址为当前地址，则直接push，无需网络请求
        if (Objects.equals(raftServer.getCurrentNodeAddress(), nodeAddress)) {
            triggerTimer.push(job);
            return;
        }
        JobProto.PushIntoTimerRequest.Builder builder = JobProto.PushIntoTimerRequest.newBuilder();
        builder.setJobId(job.getId());
        builder.setName(job.getName());
        builder.setWorkerId(job.getWorkerId());
        if (job.getStartTime() != null) {
            builder.setStartTime(LocalDateTimeUtil.toEpochMilli(job.getStartTime()));
        }
        if (job.getEndTime() != null) {
            builder.setEndTime(LocalDateTimeUtil.toEpochMilli(job.getEndTime()));
        }
        builder.setProcessorType(job.getProcessorType());
        if (job.getProcessor() != null) {
            builder.setProcessor(job.getProcessor());
        }
        if (job.getParam() != null) {
            builder.setParam(job.getParam());
        }
        builder.setTriggerType(job.getTriggerType());
        builder.setCron(job.getCron());
        if (job.getSpeedS() != null) {
            builder.setSpeedS(job.getSpeedS());
        }
        if (job.getDelayedS() != null) {
            builder.setDelayedS(job.getDelayedS());
        }
        builder.setRouteType(job.getRouteType());
        builder.setRetryCount(job.getRetryCount());
        if (job.getTimeoutS() != null) {
            builder.setTimeoutS(job.getTimeoutS());
        }
        if (job.getTimeoutHandleType() != null) {
            builder.setTimeoutHandleType(job.getTimeoutHandleType());
        }
        if (job.getLastTriggerTime() != null) {
            builder.setLastTriggerTime(job.getLastTriggerTime());
        }
        if (job.getLastExecuteEndTime() != null) {
            builder.setLastExecuteEndTime(job.getLastExecuteEndTime());
        }

        JobProto.PushIntoTimerRequest request = builder.build();
        try {
            JobProto.Response response = (JobProto.Response) raftServer.getRpcClient()
                    .invokeSync(JRaftUtils.getEndPoint(nodeAddress), request, 3000);
            if (!response.getSuccess()) {
                throw new RuntimeException("推入定时器失败");
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    private PeerId getLeader() {
        // 集群模式
        Node node =  raftServer.getNode();
        if (node == null) {
            throw new RuntimeException("JRaft服务未启动");
        }
        PeerId leaderId = node.getLeaderId();
        if (leaderId == null) {
            throw new RuntimeException("集群未就绪");
        }
        return leaderId;
    }
}
