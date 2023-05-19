package com.github.vizaizai.server.task;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.entity.Task;
import com.github.vizaizai.server.raft.RaftServer;
import com.github.vizaizai.server.raft.processor.JobPlanOpt;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.nio.ByteBuffer;

/**
 * 调度节点检查任务
 * @author liaochongwei
 * @date 2023/5/8 17:12
 */
@Slf4j
//@Component
public class NodeCheckTask {
    @Resource
    private RaftServer electionNode;

    /**
     * 一分钟扫描一次
     */
    //@Scheduled(fixedDelay = 1000 * 10)
    public void check() throws CodecException {
        // 创建并初始化 CliService
        //CliService cliService = RaftServiceFactory.createAndInitCliService(new CliOptions());
        // 使用CliService
        //Configuration conf = JRaftUtils.getConfiguration("127.0.0.1:2141,127.0.0.1:2144");
//        Status status = cliService.addPeer("vi-server", conf, new PeerId("127.0.0.1", 2144));
//        if(status.isOk()){
//            System.out.println("添加成功");
//        }

//        List<PeerId> alivePeers = cliService.getAlivePeers(Commons.serverId, conf);
//        System.out.println(alivePeers.size());


        if (electionNode.getNode() == null) {
            return;
        }
        boolean leader = electionNode.getNode().isLeader();
        if (!leader) {
            return;
        }
        Task task = new Task();
        task.setData(ByteBuffer.wrap(SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(JobPlanOpt.createPut(1,"127.0.0.1:1141"))));
        task.setDone(new Closure() {
            @Override
            public void run(Status status) {
                log.info("========================Closure:{}",status);
            }
        });
        electionNode.getNode().apply(task);

    }

}
