package com.github.vizaizai.server.router.impl;

import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.common.BizCode;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.router.NodeRouter;
import com.github.vizaizai.server.utils.RpcUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 故障转移
 * @author liaochongwei
 * @date 2023/5/23 15:36
 */
@Slf4j
public class FailoverRouter implements NodeRouter {
    @Override
    public String route(Job job, List<String> addressList) {
        for (String address : addressList) {
            RpcResponse response = RpcUtils.call(address, BizCode.BEAT,"ping");
            if (response.getSuccess()) {
                return address;
            }
        }
        return null;
    }
}
