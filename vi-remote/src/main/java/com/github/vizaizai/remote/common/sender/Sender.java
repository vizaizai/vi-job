package com.github.vizaizai.remote.common.sender;

import com.github.vizaizai.remote.client.RpcFuture;

import java.net.SocketAddress;

/**
 * 消息发送器
 * @author liaochongwei
 * @date 2023/4/23 13:53
 */
public interface Sender {

    /**
     * 直接发送
     * @param msg 消息内容
     */
    void send(Object msg);

    /**
     * 发送且返回future
     * @param requestId 请求id
     * @param msg 消息内容
     * @return RpcFuture
     */
    RpcFuture sendAndRevFuture(String requestId, Object msg);

    /**
     * 发送且等待响应（会阻塞）
     * @param requestId 请求id
     * @param msg 消息内容
     * @param timeout 超时时间 (单位: 毫秒，-1表示不限制)
     * @return Object
     */
    Object sendAndRevResponse(String requestId, Object msg, long timeout);

    /**
     * 获取远程地址
     * @return SocketAddress
     */
    SocketAddress getRemoteAddress();
}
