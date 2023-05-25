package com.github.vizaizai.server.router.impl;

import com.github.vizaizai.common.model.TaskResult;
import com.github.vizaizai.remote.common.BizCode;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.router.NodeRouter;
import com.github.vizaizai.server.utils.RpcUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 空闲路由
 * @author liaochongwei
 * @date 2023/5/23 15:36
 */
@Slf4j
public class IdleRouter implements NodeRouter {
    @Override
    public String route(Job job, List<String> addressList) {
        for (String address : addressList) {
            TaskResult taskResult = RpcUtils.toTaskResult(RpcUtils.call(address, BizCode.IDlE, job.getId()));
            if (taskResult.isSuccess()) {
                return address;
            }
        }
        return null;
    }
}
