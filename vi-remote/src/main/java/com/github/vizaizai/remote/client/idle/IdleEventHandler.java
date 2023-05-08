package com.github.vizaizai.remote.client.idle;

import com.github.vizaizai.remote.common.sender.Sender;

/**
 * 心跳检测事件处理
 * @author liaochongwei
 * @date 2023/5/8 21:00
 */
public interface IdleEventHandler {
    /**
     * 事件处理
     * @param sender 消息发送器
     */
    void handle(Sender sender);
}
