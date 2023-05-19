package com.github.vizaizai.remote.server.netty;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.server.Server;
import com.github.vizaizai.remote.server.processor.BizProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author liaochongwei
 * @date 2022/2/18 15:33
 */
public class NettyServer implements Server {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    /**
     * 地址
     */
    private final String host;
    /**
     * 绑定端口
     */
    private final int port;
    /**
     * 业务处理器
     */
    private final Map<String, BizProcessor> bizProcessorMap = new HashMap<>();


    public NettyServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void addBizProcessor(String bizCode, BizProcessor bizProcessor) {
        this.bizProcessorMap.put(bizCode, bizProcessor);
    }

    @Override
    public void start(ExecutorService executor) {
        executor.execute(() -> {
            // 一个线程来接收连接
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            // 处理器个数*2
            EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                        .childHandler(new NettyServerInitializer(bizProcessorMap))
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                ChannelFuture future = bootstrap.bind(host, port).sync();
                logger.info("Server started on port {}", port);
                future.channel().closeFuture().sync();
            }catch (Exception e) {
                logger.error("Server startup failure: ", e);
            }finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        });
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Map<String, BizProcessor> getBizProcessorMap() {
        return bizProcessorMap;
    }
}
