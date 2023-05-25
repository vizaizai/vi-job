package com.github.vizaizai.remote.client.netty;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.client.idle.IdleEventListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * netty连接池
 * @author liaochongwei
 * @date 2022/2/21 11:17
 */
public class NettyConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(NettyConnectionPool.class);
    private static final ChannelHealthChecker healthCheck = ChannelHealthChecker.ACTIVE;
    // 新建策略：当检测到获取连接超时时，此时新建一个连接
    private static final FixedChannelPool.AcquireTimeoutAction acquireTimeoutAction = FixedChannelPool.AcquireTimeoutAction.NEW;
    private static final int acquireTimeoutMillis = 1000 * 60;
    private static final int maxConnect = 1;
    private static final int maxPendingAcquires = 5;
    private final Bootstrap bootstrap = new Bootstrap();
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
    private ChannelPoolMap<InetSocketAddress, SimpleChannelPool> poolMap;
    // 心跳检查监听器
    private static final Map<InetSocketAddress, IdleEventListener> idleEventListenerMap = new HashMap<>();
    private static volatile NettyConnectionPool nettyConnectionPool = null;

    public static synchronized NettyConnectionPool getInstance() {
        if (nettyConnectionPool == null) {
            synchronized (NettyConnectionPool.class) {
                if (nettyConnectionPool == null) {
                    nettyConnectionPool = new NettyConnectionPool();
                }
            }
        }
        return nettyConnectionPool;
    }

    private NettyConnectionPool() {
        this.init();
    }

    public void init() {
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);

        poolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
            @Override
            protected SimpleChannelPool newPool(InetSocketAddress key) {
                return new FixedChannelPool(bootstrap.remoteAddress(key), new NettyChannelPoolHandler(idleEventListenerMap.get(key)),
                        healthCheck,acquireTimeoutAction, acquireTimeoutMillis, maxConnect, maxPendingAcquires,true,true);
            }
        };
    }

    /**
     * 获取连接
     * @param inetSocketAddress 地址
     * @return
     */
    public Channel acquire(InetSocketAddress inetSocketAddress) {
        try {
            Future<Channel> fch = poolMap.get(inetSocketAddress).acquire();
            fch.addListener((FutureListener<Channel>) e -> {
                if (e.isSuccess()) {
                    logger.debug("Successfully connect to remote server:{}", inetSocketAddress);
                }else {
                    logger.error("Can not connect to remote server:{}", inetSocketAddress);
                }
            });
            return fch.get();
        } catch (Exception e) {
            logger.error("Acquire netty connection error. ",e);
            throw new RuntimeException("Acquire netty connection error：" + e.getMessage());
        }
    }

    /**
     * 释放连接
     * @param channel 通道
     * @param inetSocketAddress 地址
     */
    public void release(Channel channel, InetSocketAddress inetSocketAddress) {
        try {
            if (channel != null) {
                poolMap.get(inetSocketAddress).release(channel);
            }
        } catch (Exception e) {
            logger.error("Release netty connection error.",e);
        }
    }

    public void remove(InetSocketAddress inetSocketAddress) {
        try {
            ((AbstractChannelPoolMap<InetSocketAddress,SimpleChannelPool>) poolMap).remove(inetSocketAddress);
        } catch (Exception e) {
            logger.error("Remove pool error,",e);
        }
    }

    /**
     * 注册心跳检查事件处理
     * @param listener
     */
    public static void registerIdleListener(InetSocketAddress inetSocketAddress, IdleEventListener listener) {
        idleEventListenerMap.put(inetSocketAddress, listener);
    }
}
