package com.github.vizaizai.remote.client.netty;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.HeartBeat;
import com.github.vizaizai.remote.common.sender.NettySender;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.net.SocketAddress;

/*
 * netty客户端处理器
 * @author liaochongwei
 * @date 2022/2/21 10:22
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    private volatile Channel channel;
    private SocketAddress remotePeer;
    private NettySender nettySender;
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
        this.nettySender = new NettySender(this.channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Exception: {}", cause.getMessage());
        ctx.close();
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
            RpcRequest request = RpcRequest.wrap(HeartBeat.CODE, "ping");
            logger.debug("[{}_{}]PING",remotePeer,request.getRequestId());
            try {
                nettySender.send(request);
            }catch (Exception e) {
                logger.warn("[{}] send error:",request.getRequestId(),e);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.warn("{} disconnect", ctx.channel().remoteAddress());
    }
}
