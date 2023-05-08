package com.github.vizaizai.remote.client;

import com.github.vizaizai.remote.client.idle.IdleEventHandler;
import com.github.vizaizai.remote.common.sender.Sender;

import java.util.function.Supplier;

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
    /**
     * 设置心跳检测事件处理器getter
     */
    default void setIdleEventHandlerGetter(Supplier<IdleEventHandler> getter) {
    }
}
