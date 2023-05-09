package com.github.vizaizai.remote.client;

import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;

import java.net.InetSocketAddress;

/**
 * 客户端
 * @author liaochongwei
 * @date 2022/2/21 10:09
 */
public interface Client {

    RpcResponse request(RpcRequest request,long timeout);
    void destroy();
    default InetSocketAddress getAddress(){
        return null;
    }
}
