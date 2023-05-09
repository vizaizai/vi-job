package com.github.vizaizai.remote.server.processor;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * 心跳处理
 * @author liaochongwei
 * @date 2023/4/23 16:06
 */
public class HeartBeatProcessor implements BizProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatProcessor.class);
    @Override
    public void execute(RpcRequest request, Sender sender) {
        String action = (String) request.getParam();
        if (Objects.equals(action,"ping")) {
            logger.info("[{}_{}]PONG",sender.getRemoteAddress(),request.getRequestId());
            sender.send(RpcResponse.ok(request.getRequestId(),"pong"));
        }
    }
}
