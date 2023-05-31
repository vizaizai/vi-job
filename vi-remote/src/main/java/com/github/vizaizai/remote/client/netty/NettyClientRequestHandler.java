package com.github.vizaizai.remote.client.netty;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.common.sender.NettySender;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/*
 * netty客户端处理器
 * @author liaochongwei
 * @date 2022/2/21 10:22
 */
public class NettyClientRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientRequestHandler.class);
    private NettySender nettySender;
    private static final Executor executor = Executors.newSingleThreadExecutor();


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        Channel channel = ctx.channel();
        this.nettySender = new NettySender(channel);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        logger.info(">>>>>>>>>>>客户端收到request:{}",request);
    }

    public NettySender getNettySender() {
        return nettySender;
    }
}
