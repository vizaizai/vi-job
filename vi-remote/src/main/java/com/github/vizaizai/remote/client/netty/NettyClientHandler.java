package com.github.vizaizai.remote.client.netty;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.client.event.IdleEventListener;
import com.github.vizaizai.remote.codec.RpcMessage;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.NettySender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import io.netty.channel.Channel;
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

/*
 * netty客户端处理器
 * @author liaochongwei
 * @date 2022/2/21 10:22
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    private NettySender nettySender;
    private final IdleEventListener idleEventListener;
    /**
     * 业务处理器
     */
    private final Map<String, BizProcessor> bizProcessorMap;
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

    public NettyClientHandler(IdleEventListener idleEventListener, Map<String, BizProcessor> bizProcessorMap) {
        this.idleEventListener = idleEventListener;
        this.bizProcessorMap = bizProcessorMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        Channel channel = ctx.channel();
        this.nettySender = new NettySender(channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        logger.warn("Client cause exception:{}", cause.getMessage());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage message) throws Exception {
        // 接收响应
        if (Objects.equals(message.getDirection(), RpcMessage.RESPONSE)) {
            if (StringUtils.isNotBlank(message.getTraceId())) {
                NettySender.done(message.getTraceId(), message.getResponse());
            }
        }else {// 接收请求
            RpcRequest rpcRequest = message.getRequest();
            // 异步执行
            try {
                bizExecutor.execute(()->{
                    String bizCode = rpcRequest.getBizCode();
                    BizProcessor bizProcessor = bizProcessorMap.get(bizCode);
                    if (bizProcessor != null) {
                        bizProcessor.execute(rpcRequest, nettySender);
                    }else {
                        nettySender.send(RpcMessage.createResponse(message.getTraceId(), RpcResponse.error("Processor is not Found")));
                    }
                });
            }catch (Exception e) {
                logger.error("Execute error,", e);
                nettySender.send(RpcMessage.createResponse(message.getTraceId(), RpcResponse.error(e.getMessage())));
            }
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (this.idleEventListener != null) {
                bizExecutor.execute(()-> this.idleEventListener.complete(this.nettySender));
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.warn("Channel[{}] inactive,remote: {}",ctx.channel().id(), ctx.channel().remoteAddress());

    }

    public NettySender getNettySender() {
        return nettySender;
    }
}
