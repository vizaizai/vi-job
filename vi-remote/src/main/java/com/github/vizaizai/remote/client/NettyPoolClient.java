package com.github.vizaizai.remote.client;

import com.github.vizaizai.remote.client.netty.NettyConnectionPool;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.NettySender;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.remote.utils.NetUtils;
import io.netty.channel.Channel;
import org.apache.commons.lang3.tuple.Pair;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * netty-pool客户端
 * @author liaochongwei
 * @date 2023/4/20 11:32
 */
public class NettyPoolClient implements Client{
    private  static final Map<String, NettyPoolClient> clients = new ConcurrentHashMap<>();
    private final InetSocketAddress inetSocketAddress;
    private Channel channel;
    private NettyPoolClient(String address) {
        Pair<String, Integer> netPair = NetUtils.splitAddress2IpAndPort(address);
        inetSocketAddress = new InetSocketAddress(netPair.getKey(), netPair.getValue());
    }

    public static NettyPoolClient getInstance(String address) {
        NettyPoolClient nettyPoolClient = clients.get(address);
        if (nettyPoolClient == null) {
            nettyPoolClient = new NettyPoolClient(address);
            clients.put(address, nettyPoolClient);
        }
        return nettyPoolClient;
    }

    @Override
    public RpcResponse request(RpcRequest request, long timeout) {
        // 获取发送器
        Sender sender = this.createSender();
        // 发送请求并且等待返回
       try {
           return (RpcResponse) sender.sendAndRevResponse(request, timeout);
       }finally {
           // 释放
           release();
       }
    }

    @Override
    public void destroy() {
        NettyConnectionPool.getInstance().remove(this.inetSocketAddress);
    }

    private Sender createSender() {
        channel = NettyConnectionPool.getInstance().acquire(inetSocketAddress);
        return new NettySender(channel);
    }

    private void release() {
        NettyConnectionPool.getInstance().release(this.channel, inetSocketAddress);
    }

    @Override
    public InetSocketAddress getAddress(){
        return this.inetSocketAddress;
    }
}
