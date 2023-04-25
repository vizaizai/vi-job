package com.github.vizaizai.remote.client;

import com.github.vizaizai.remote.common.sender.Sender;

/**
 * 客户端
 * @author liaochongwei
 * @date 2022/2/21 10:09
 */
public interface Client {

    /**
     * 连接
     */
    Sender connect();

    /**
     * 断开连接
     */
    void disconnect();



}
