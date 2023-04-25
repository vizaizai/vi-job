package com.github.vizaizai.remote.client;

import com.github.vizaizai.remote.client.netty.NettyConnectionPool;
import com.github.vizaizai.remote.common.sender.NettySender;
import com.github.vizaizai.remote.common.sender.Sender;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * netty-pool客户端
 * @author liaochongwei
 * @date 2023/4/20 11:32
 */
public class NettyPoolClient implements Client{
    private final InetSocketAddress inetSocketAddress;
    private Channel channel;
    private static final NettyConnectionPool nettyConnectionPool;

    static {
        nettyConnectionPool = new NettyConnectionPool();
    }

    public NettyPoolClient(String host, int port) {
        inetSocketAddress = new InetSocketAddress(host, port);
    }
    @Override
    public Sender connect() {
        channel = nettyConnectionPool.acquire(inetSocketAddress);
        return new NettySender(channel);
    }

    @Override
    public void disconnect() {
        nettyConnectionPool.release(this.channel, inetSocketAddress);
    }

}
