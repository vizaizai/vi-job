package com.github.vizaizai.server.utils;

import cn.hutool.json.JSONUtil;
import com.github.vizaizai.common.model.TaskResult;
import com.github.vizaizai.remote.client.NettyPoolClient;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.codec.RpcResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Rpc工具
 * @author liaochongwei
 * @date 2023/5/22 19:18
 */
@Slf4j
public class RpcUtils {

    public static TaskResult toTaskResult(RpcResponse response) {
        if (!response.getSuccess()) {
            return TaskResult.fail(response.getMsg());
        }
        if (response.getResult() == null || !(response.getResult() instanceof TaskResult)) {
            log.error("toTaskResult:{}", JSONUtil.toJsonStr(response));
            return TaskResult.fail("Internal error");
        }
        return (TaskResult) response.getResult();
    }

    /**
     * 远程调用
     * @param address ip:port
     * @param bizCode 业务码
     * @param param 参数
     * @return RpcResponse
     */
    public static RpcResponse call(String address, String bizCode, Object param) {
        RpcRequest request = RpcRequest.wrap(bizCode, param);
        try {
            return NettyPoolClient.getInstance(address).request(request, 30000);
        }catch (Exception e) {
            return RpcResponse.error(e.getMessage());
        }
    }
}
