package com.github.vizaizai.server.router.impl;

import cn.hutool.core.collection.CollUtil;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.router.NodeRouter;

import java.util.List;

/**
 * 广播路由
 * @author liaochongwei
 * @date 2023/5/22 17:26
 */
public class BroadcastRouter implements NodeRouter {
    @Override
    public String route(Job job, List<String> addressList) {
        return CollUtil.join(addressList,",");
    }
}
