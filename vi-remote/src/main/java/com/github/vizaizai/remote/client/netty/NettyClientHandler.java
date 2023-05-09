package com.github.vizaizai.remote.client.netty;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.client.idle.IdleEventListener;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.NettySender;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/*
 * netty客户端处理器
 * @author liaochongwei
 * @date 2022/2/21 10:22
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    private NettySender nettySender;
    private IdleEventListener idleEventListener;
    private static final Executor executor = Executors.newSingleThreadExecutor();

    public NettyClientHandler() {
    }
    public NettyClientHandler(IdleEventListener idleEventListener) {
        this.idleEventListener = idleEventListener;
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
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        if (StringUtils.isNotBlank(response.getRequestId())) {
            NettySender.done(response.getRequestId(), response);
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (this.idleEventListener != null) {
                executor.execute(()-> this.idleEventListener.complete(this.nettySender));
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
