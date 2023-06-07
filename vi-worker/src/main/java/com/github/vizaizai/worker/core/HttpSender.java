package com.github.vizaizai.worker.core;

import com.github.vizaizai.remote.client.RpcFuture;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.worker.utils.HttpUtils;

import java.net.SocketAddress;

/**
 * @author liaochongwei
 * @date 2023/6/1 17:02
 */
public class HttpSender implements Sender {

    private final String url;

    public HttpSender(String url) {
        this.url = url;
    }

    @Override
    public void send(Object msg) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public RpcFuture sendAndRevFuture(Object msg) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public Object sendAndRevResponse(Object msg, long timeout) {
        return HttpUtils.doPost(this.url, msg, timeout);
    }

    @Override
    public SocketAddress getRemoteAddress() {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public boolean available() {
        return true;
    }
}
