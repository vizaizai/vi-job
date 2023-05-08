package com.github.vizaizai.remote.client;

import com.github.vizaizai.remote.client.idle.IdleEventHandler;
import com.github.vizaizai.remote.client.netty.NettyConnectionPool;
import com.github.vizaizai.remote.common.sender.NettySender;
import com.github.vizaizai.remote.common.sender.Sender;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

/**
 * netty-pool客户端
 * @author liaochongwei
 * @date 2023/4/20 11:32
 */
public class NettyPoolClient implements Client{
    private final InetSocketAddress inetSocketAddress;
    private Channel channel;
    Supplier<IdleEventHandler> getter;
    public NettyPoolClient(String host, int port) {
        inetSocketAddress = new InetSocketAddress(host, port);
    }

    @Override
    public void setIdleEventHandlerGetter(Supplier<IdleEventHandler> getter) {
        this.getter = getter;
    }

    @Override
    public Sender connect() {
        channel = NettyConnectionPool.getInstance(getter).acquire(inetSocketAddress);
        return new NettySender(channel);
    }

    @Override
    public void disconnect() {
        NettyConnectionPool.getInstance().release(this.channel, inetSocketAddress);
    }

}
