package com.github.vizaizai.server.launch;

import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.remote.client.idle.IdleEventListener;
import com.github.vizaizai.remote.client.netty.NettyConnectionPool;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
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
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 注册任务上报处理器
        NettyConnectionPool.registerProcessor(BizCode.REPORT, jobReportProcessor);
        // 设置心跳监听处理
        NettyConnectionPool.setIdleEventListener(sender -> sender.send(RpcMessage.createRequest(RpcRequest.wrap(BizCode.BEAT, "ping"))));
    }
}
