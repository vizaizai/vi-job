package com.github.vizaizai.remote.server.netty;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.remote.common.sender.NettySender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import com.github.vizaizai.remote.server.processor.DefaultProcessor;
import com.github.vizaizai.remote.server.processor.HeartBeatProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 服务器端处理器
 * @author liaochongwei
 * @date 2022/2/18 11:28
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private final Map<String, BizProcessor> bizProcessorMap;
    private NettySender nettySender;
    /**
     * 心跳检查
     */
    private final BizProcessor heartBeatProcessor;
    /**
     * 默认处理
     */
    private final BizProcessor defaultProcessor;
    /**
     * 业务处理执行线程池
     */
    private static final Executor bizExecutor = new ThreadPoolExecutor(
            0,
            200,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2000),
            new BasicThreadFactory.Builder().namingPattern("Netty-processor-%d").build(),
            (r, executor) -> {
                throw new RuntimeException("vi-job, Netty processor-pool is exhausted!");
            });

    public NettyServerHandler(Map<String, BizProcessor> bizProcessorMap) {
        this.bizProcessorMap = bizProcessorMap;
        this.heartBeatProcessor = new HeartBeatProcessor();
        this.defaultProcessor = new DefaultProcessor();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage message) throws Exception {
        // 响应
        if (Objects.equals(message.getDirection(), RpcMessage.RESPONSE)) {
            if (StringUtils.isNotBlank(message.getTraceId())) {
                NettySender.done(message.getTraceId(), message.getResponse());
            }
        }else {
            // 请求
            if (nettySender == null) {
                nettySender = new NettySender(ctx.channel());
            }
            RpcRequest rpcRequest = message.getRequest();
            // 异步执行
            try {
                bizExecutor.execute(()->{
                    String bizCode = rpcRequest.getBizCode();
                    BizProcessor bizProcessor = bizProcessorMap.get(bizCode);
                    if (bizProcessor != null) {
                        bizProcessor.execute(rpcRequest, nettySender);
                        return;
                    }
                    if (Objects.equals(BizCode.BEAT, bizCode)) {
                        heartBeatProcessor.execute(rpcRequest, nettySender);
                        return;
                    }
                    // 执行默认处理器
                    defaultProcessor.execute(rpcRequest, nettySender);
                });
            }catch (Exception e) {
                logger.error("Execute error,", e);
                nettySender.send(RpcMessage.createResponse(message.getTraceId(), RpcResponse.error(e.getMessage())));
            }
        }




    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.warn("{} down",ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        logger.warn("Server cause exception:{}", cause.getMessage());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
            logger.warn("Channel idle in last {} seconds, close it", 90);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.debug("{} up", ctx.channel().remoteAddress());
    }
}
