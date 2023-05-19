package com.github.vizaizai.remote.client;

import com.github.vizaizai.remote.client.netty.NettyConnectionPool;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.HeartBeat;
import com.github.vizaizai.remote.common.sender.NettySender;
import com.github.vizaizai.remote.common.sender.Sender;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * netty-pool客户端
 * @author liaochongwei
 * @date 2023/4/20 11:32
 */
public class NettyPoolClient implements Client{
    private  final static Map<String, NettyPoolClient> clients = new HashMap<>();
    private final InetSocketAddress inetSocketAddress;
    private Channel channel;
    private NettyPoolClient(String host, int port) {
        inetSocketAddress = new InetSocketAddress(host, port);
        NettyConnectionPool.registerIdleListener(inetSocketAddress, sender -> {
            RpcRequest request = RpcRequest.wrap(HeartBeat.CODE, null);
            try {
                sender.send(request);
            }catch (Exception ignored) {
            }
        });
    }

    public static NettyPoolClient getInstance(String host, int port) {
        String key = host + ":" + port;
        NettyPoolClient nettyPoolClient = clients.get(key);
        if (nettyPoolClient == null) {
            nettyPoolClient = new NettyPoolClient(host, port);
            clients.put(key, nettyPoolClient);
        }
        return nettyPoolClient;
    }


    @Override
    public RpcResponse request(RpcRequest request,long timeout) {
        // 获取发送器
        Sender sender = this.createSender();
        // 发送请求并且等待返回
       try {
           return (RpcResponse) sender.sendAndRevResponse(request.getRequestId(), request, timeout);
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
    public InetSocketAddress getAddress() {
        return this.inetSocketAddress;
    }
}
