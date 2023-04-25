package com.github.vizaizai.remote.common.sender;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.client.RpcFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * netty消息发送器
 * @author liaochongwei
 * @date 2023/4/23 13:55
 */
public class NettySender implements Sender{

    private static final Logger logger = LoggerFactory.getLogger(NettySender.class);
    /**
     * 等待响应Future
     */
    private static final Map<String, RpcFuture> pendingFutures = new ConcurrentHashMap<>();
    /**
     * netty
     */
    private final Channel channel;

    public NettySender(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void send(Object msg) {
        try {
            ChannelFuture channelFuture = channel.writeAndFlush(msg).sync();
            if (!channelFuture.isSuccess()) {
                logger.error("Send request error[{}]", msg);
            }
        } catch (InterruptedException e) {
            logger.error("Send request exception: " + e.getMessage());
        }
    }

    @Override
    public RpcFuture sendAndRevFuture(String requestId, Object msg) {
        RpcFuture rpcFuture = new RpcFuture(requestId);
        pendingFutures.put(requestId, rpcFuture);
        try {
            ChannelFuture channelFuture = channel.writeAndFlush(msg).sync();
            if (!channelFuture.isSuccess()) {
                logger.error("Send request error[{}]", msg);
            }
        } catch (InterruptedException e) {
            logger.error("Send request exception: " + e.getMessage());
        }
        return rpcFuture;
    }

    @Override
    public Object sendAndRevResponse(String requestId, Object msg, long timeout) {
        RpcFuture rpcFuture = this.sendAndRevFuture(requestId, msg);
        try {
            if (timeout <= -1) {
                return rpcFuture.get();
            }
            return rpcFuture.get(timeout, TimeUnit.MILLISECONDS);
        }catch (InterruptedException e) {
            logger.error("RpcFuture.get() error:",e);
            throw new RuntimeException("RpcFuture.get() error: " + e.getMessage());
        }
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }

    /**
     * 响应完成
     * @param requestId 请求id
     * @param response 响应
     */
    public static void done(String requestId, Object response) {
        // 获取阻塞中的请求
        RpcFuture rpcFuture = pendingFutures.get(requestId);
        if (rpcFuture != null) {
            // 移除并且完成
            pendingFutures.remove(requestId);
            rpcFuture.done(response);
        }
    }

    public Channel getChannel() {
        return channel;
    }
}
