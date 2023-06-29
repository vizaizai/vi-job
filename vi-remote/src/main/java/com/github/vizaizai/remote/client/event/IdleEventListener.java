package com.github.vizaizai.remote.client.event;

import com.github.vizaizai.remote.common.sender.Sender;

/**
 * 心跳检测事件监听器
 * @author liaochongwei
 * @date 2023/5/8 21:00
 */
public interface IdleEventListener {
    /**
     * 事件处理
     * @param sender 消息发送器
     */
    void complete(Sender sender);
}
