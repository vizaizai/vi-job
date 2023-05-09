package com.github.vizaizai.remote.client;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.client.netty.NettyClientHandler;
import com.github.vizaizai.remote.client.netty.NettyClientInitializer;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.NettySender;
import com.github.vizaizai.remote.common.sender.Sender;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

/**
 * netty客户端
 * @author liaochongwei
 * @date 2023/4/20 11:32
 */
@Deprecated
public class NettyClient implements Client{
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private EventLoopGroup eventLoopGroup;

    /**
     * 地址
     */
    private final String host;
    /**
     * 绑定端口
     */
    private final int port;

    private final Bootstrap bootstrap = new Bootstrap();
    /**
     * 启动线程池
     */
    private final Executor startExecutor;
    // 客户端处理器
    private NettyClientHandler nettyClientHandler;
    public NettyClient(String host, int port, EventLoopGroup eventLoopGroup, Executor startExecutor) {
        this.host = host;
        this.port = port;
        this.eventLoopGroup = eventLoopGroup;
        this.startExecutor = startExecutor;
        this.bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
        this.connect();
    }
    private void connect() {
        final InetSocketAddress addr = new InetSocketAddress(this.host, this.port);
        startExecutor.execute(()->{
            ChannelFuture channelFuture = bootstrap.connect(addr);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        logger.info("Successfully connect to remote server:{}",addr);
                        nettyClientHandler = channelFuture.channel().pipeline().get(NettyClientHandler.class);
                    } else {
                        logger.error("Can not connect to remote server: " + addr);
                    }
                }
            });
            try {
                channelFuture.channel().closeFuture().sync();
            }catch (InterruptedException ie) {
                logger.error("Close future error.", ie);
            }
        });
    }

    @Override
    public RpcResponse request(RpcRequest request, long timeout) {
        if (this.nettyClientHandler == null) {
            throw new RuntimeException("Netty Client is not ready.");
        }
        return (RpcResponse) this.nettyClientHandler.getNettySender().sendAndRevResponse(request.getRequestId(), request, timeout);
    }

    @Override
    public void destroy() {
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    public void setEventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public NettyClientHandler getNettyClientHandler() {
        return nettyClientHandler;
    }


}
