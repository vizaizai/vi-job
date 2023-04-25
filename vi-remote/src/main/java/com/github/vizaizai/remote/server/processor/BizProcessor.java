package com.github.vizaizai.remote.server.processor;

import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.common.sender.Sender;

/**
 * 业务处理
 * @author liaochongwei
 * @date 2023/4/23 16:05
 */
public interface BizProcessor {
    void execute(RpcRequest request, Sender sender);
}
