package com.github.vizaizai.server.launch;

import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.remote.client.NettyClient;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.server.raft.RaftNodeOptions;
import com.github.vizaizai.server.raft.RaftServer;
import com.github.vizaizai.server.service.JobReportProcessor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * app启动
 * @author liaochongwei
 * @date 2023/6/7 14:55
 */
@Component
public class AppLaunch implements ApplicationRunner {
    @Resource
    private JobReportProcessor jobReportProcessor;
    @Resource
    private RaftServer raftServer;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 注册任务上报处理器
        NettyClient.registerProcessor(BizCode.REPORT, jobReportProcessor);
        // 设置心跳监听处理
        NettyClient.setIdleEventListener(sender -> sender.send(RpcMessage.createRequest(RpcRequest.wrap(BizCode.BEAT, "ping"))));
        // 初始化raft服务
        raftServer.init(new RaftNodeOptions());
    }
}
