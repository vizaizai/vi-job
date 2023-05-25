package com.github.vizaizai.remote.server.processor;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.sender.Sender;
import org.slf4j.Logger;

/**
 * 默认处理器
 * @author liaochongwei
 * @date 2023/4/23 16:06
 */
public class DefaultProcessor implements BizProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessor.class);
    @Override
    public void execute(RpcRequest request, Sender sender) {
        logger.debug("[{}_{}]{}",sender.getRemoteAddress(),request.getRequestId(),request);
        sender.send(RpcResponse.error(request.getRequestId(),"Not found!"));
    }
}
