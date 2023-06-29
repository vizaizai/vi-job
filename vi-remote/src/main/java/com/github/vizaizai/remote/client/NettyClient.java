package com.github.vizaizai.remote.client;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.client.event.IdleEventListener;
import com.github.vizaizai.remote.client.netty.NettyClientHandler;
import com.github.vizaizai.remote.client.netty.NettyClientInitializer;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.NettySender;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import com.github.vizaizai.remote.utils.NetUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * netty客户端
 * @author liaochongwei
 * @date 2023/4/20 11:32
 */
public class NettyClient implements Client{
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    /**
     * 地址
     */
    private final String host;
    /**
     * 绑定端口
     */
    private final int port;

    private final Bootstrap bootstrap = new Bootstrap();
    private ChannelFuture channelFuture = null;
    /**
     * 客户端处理器
     */
    private NettyClientHandler nettyClientHandler = null;
    /**
     * 启动CountDownLatch
     */
    private CountDownLatch startCountDownLatch = null;
    /**
     * 是否已连接
     */
    private boolean connected;
    /**
     * 连接池
     */
    private static final Map<String, NettyClient> clients = new ConcurrentHashMap<>();
    /**
     * 心跳检查监听器
     */
    private static IdleEventListener idleEventListener = null;
    /**
     * 业务处理器
     */
    private static final Map<String, BizProcessor> bizProcessorMap = new HashMap<>();

    public static NettyClient getInstance(String address) {
        NettyClient nettyClient = clients.get(address);
        if (nettyClient == null) {
            Pair<String, Integer> pair = NetUtils.splitAddress2IpAndPort(address);
            nettyClient = new NettyClient(pair.getKey(), pair.getValue());
            clients.put(address, nettyClient);
        }
        return nettyClient;
    }

    private NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer(idleEventListener, bizProcessorMap));
        this.connect();
    }
    private void connect() {
        this.connected = false;
        this.startCountDownLatch = new CountDownLatch(1);
        try {
            final InetSocketAddress addr = new InetSocketAddress(this.host, this.port);
            this.channelFuture = bootstrap.connect(addr);
            this.channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        logger.info("Successfully connect to remote server:{}",addr);
                        nettyClientHandler = channelFuture.channel().pipeline().get(NettyClientHandler.class);
                        connected = true;
                        startCountDownLatch.countDown();
                    } else {
                        logger.error("Can not connect to remote server: " + addr);
                        startCountDownLatch.countDown();
                    }
                }
            });
        }catch (Exception e) {
            startCountDownLatch.countDown();
        }
    }

    @Override
    public RpcResponse request(RpcRequest request, long timeout) {
        if (!this.connected()) {
            if (!this.connectSync()) {
                return RpcResponse.error("Connection timeout");
            }
        }
        NettySender nettySender = this.nettyClientHandler.getNettySender();
        // 断线，尝试连接
        if (!nettySender.available()) {
            if (!this.connectSync()) {
                return RpcResponse.error("Connection timeout");
            }
            nettySender = this.nettyClientHandler.getNettySender();
        }
        return (RpcResponse) nettySender.sendAndRevResponse(request, timeout);
    }


    @Override
    public void destroy() {
        if (this.connected()) {
            this.channelFuture.channel().close();
        }
        clients.remove(this.host + ":" + this.port);
    }

    /**
     * 同步连接
     * @return boolean
     */
    private boolean connectSync() {
        this.connect();
        return this.connected();
    }
    /**
     * 检查是否连接，若正在连接，则会等待连接后返回
     * @return boolean
     */
    private boolean connected() {
        if (!this.connected) {
            try {
                boolean await = startCountDownLatch.await(3, TimeUnit.SECONDS);
                if (!await || !this.connected) {
                    return false;
                }
            }catch (InterruptedException e) {
                logger.error("Waiting to be interrupted,{}",e.getMessage());
                return false;
            }
        }
        return true;
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public NettyClientHandler getNettyClientHandler() {
        return nettyClientHandler;
    }

    /**
     * 注册心跳检查事件处理
     * @param listener 监听器
     */
    public static void setIdleEventListener(IdleEventListener listener) {
        idleEventListener = listener;
    }

    /**
     * 注册处理器
     * @param bizCode 业务码
     * @param processor 处理器
     */
    public static void registerProcessor(String bizCode, BizProcessor processor) {
        bizProcessorMap.put(bizCode, processor);
    }
}
