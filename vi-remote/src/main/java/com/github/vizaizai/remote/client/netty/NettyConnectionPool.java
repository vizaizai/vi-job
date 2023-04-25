package com.github.vizaizai.remote.client.netty;

import com.github.vizaizai.logging.LoggerFactory;
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
    private static final int maxConnect = 2000;
    private static final int maxPendingAcquires = 100000;
    private final Bootstrap bootstrap = new Bootstrap();
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
    private ChannelPoolMap<InetSocketAddress, SimpleChannelPool> poolMap;

    public NettyConnectionPool() {
        this.init();
    }

    public void init() {
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);

        poolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
            @Override
            protected SimpleChannelPool newPool(InetSocketAddress key) {
                return new FixedChannelPool(bootstrap.remoteAddress(key), new NettyChannelPoolHandler(),
                        healthCheck,acquireTimeoutAction, acquireTimeoutMillis, maxConnect,maxPendingAcquires,true,true);
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
                    logger.info("Successfully connect to remote server:{}", inetSocketAddress);
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
}
