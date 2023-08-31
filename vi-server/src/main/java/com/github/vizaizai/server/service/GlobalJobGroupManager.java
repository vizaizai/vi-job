package com.github.vizaizai.server.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.entity.PeerId;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.raft.RaftServer;
import com.github.vizaizai.server.raft.proto.JobProto;
import com.github.vizaizai.server.raft.proto.ResponseProto;
import com.github.vizaizai.server.service.apply.JobAssignApplyService;
import com.github.vizaizai.server.timer.JobTriggerTimer;
import com.github.vizaizai.server.timer.watch.WatchDogRunner;
import com.github.vizaizai.server.timer.watch.WatchInstance;
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
    private JobAssignApplyService jobAssignApplyService;
    /**
     * 任务分配
     * @param jobId 任务id
     */
    public String assign(Long jobId) {
        // 单机模式无需分组
        if (!raftServer.isCluster()) {
            return null;
        }
        PeerId leaderId = raftServer.getLeader();
        JobProto.AssignRequest request = JobProto.AssignRequest.newBuilder().setJobId(jobId).build();
        try {
            ResponseProto.Response response = (ResponseProto.Response) raftServer.getRpcClient()
                    .invokeSync(leaderId.getEndpoint(), request, 3000);
            if (!response.getSuccess()) {
                throw new RuntimeException(response.getErrorMsg());
            }
            String data = response.getData();
            log.info("任务分配结果：{}->{}",jobId, data);
            return data;
        }catch (Exception e) {
            log.error("任务分配失败: {}",e.getMessage());
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
        PeerId leaderId = raftServer.getLeader();
        JobProto.RmRequest request = JobProto.RmRequest.newBuilder().setJobId(jobId).build();
        try {
            ResponseProto.Response response = (ResponseProto.Response) raftServer
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
     * @param job 任务信息
     */
    public void pushIntoTimer(Job job) {
        if (!raftServer.isCluster()) {
            JobTriggerTimer.getInstance().push(job);
            return;
        }

        String nodeAddress = jobAssignApplyService.get(job.getId(), true);
        if (Utils.isBlank(nodeAddress)) {
            throw new RuntimeException("任务未分配，请稍等");
        }
        // 执行调度地址为当前地址，则直接push，无需网络请求
        if (Objects.equals(raftServer.getCurrentNodeAddress(), nodeAddress)) {
            JobTriggerTimer.getInstance().push(job);
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
        if (job.getCron() != null) {
            builder.setCron(job.getCron());
        }
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
        if (job.getMaxWaitNum() != null) {
            builder.setMaxWaitNum(job.getMaxWaitNum());
        }
        if (job.getLogAutoDelHours() != null) {
            builder.setLogAutoDelHours(job.getLogAutoDelHours());
        }
        if (job.getNextTriggerTime() != null) {
            builder.setNextTriggerTime(job.getNextTriggerTime());
        }
        builder.setDirectRun(job.isDirectRun());
        if (job.getInstanceId() != null) {
            builder.setInstanceId(job.getInstanceId());
        }

        JobProto.PushIntoTimerRequest request = builder.build();
        try {
            ResponseProto.Response response = (ResponseProto.Response) raftServer.getRpcClient()
                    .invokeSync(JRaftUtils.getEndPoint(nodeAddress), request, 3000);
            if (!response.getSuccess()) {
                throw new RuntimeException("推入定时器失败");
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 从定时器中移除
     * @param jobId 任务id
     */
    public void removeFormTimer(Long jobId) {
        if (!raftServer.isCluster()) {
            JobTriggerTimer.getInstance().remove(jobId);
            return;
        }

        String nodeAddress = jobAssignApplyService.get(jobId, false);
        if (Utils.isBlank(nodeAddress)) {
            throw new RuntimeException("任务未分配，请稍等");
        }
        // 执行调度地址为当前地址，则直接remove，无需网络请求
        if (Objects.equals(raftServer.getCurrentNodeAddress(), nodeAddress)) {
            JobTriggerTimer.getInstance().remove(jobId);
            return;
        }
        JobProto.RemoveFromTimerRequest.Builder builder = JobProto.RemoveFromTimerRequest.newBuilder();
        builder.setJobId(jobId);
        JobProto.RemoveFromTimerRequest request = builder.build();
        try {
            ResponseProto.Response response = (ResponseProto.Response) raftServer.getRpcClient()
                    .invokeSync(JRaftUtils.getEndPoint(nodeAddress), request, 3000);
            if (!response.getSuccess()) {
                throw new RuntimeException("从定时器移除失败");
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 任务执行结束监听处理
     * @param jobId 任务id
     */
    public void endWatchForJobExec(Long jobId) {
        if (!raftServer.isCluster()) {
            WatchDogRunner.getInstance().end(WatchInstance.getWatchId1(jobId));
            return;
        }

        String nodeAddress = jobAssignApplyService.get(jobId, false);
        if (Utils.isBlank(nodeAddress)) {
            throw new RuntimeException("任务未分配，请稍等");
        }
        // 执行调度地址为当前地址，无需网络请求
        if (Objects.equals(raftServer.getCurrentNodeAddress(), nodeAddress)) {
            WatchDogRunner.getInstance().end(WatchInstance.getWatchId1(jobId));
            return;
        }
        JobProto.EndWatchForJobExecRequest.Builder builder = JobProto.EndWatchForJobExecRequest.newBuilder();
        builder.setJobId(jobId);
        JobProto.EndWatchForJobExecRequest request = builder.build();
        try {
            ResponseProto.Response response = (ResponseProto.Response) raftServer.getRpcClient()
                    .invokeSync(JRaftUtils.getEndPoint(nodeAddress), request, 3000);
            if (!response.getSuccess()) {
                throw new RuntimeException("任务执行结束监听处理失败");
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
