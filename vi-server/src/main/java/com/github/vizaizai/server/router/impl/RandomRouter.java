package com.github.vizaizai.server.router.impl;

import cn.hutool.core.util.RandomUtil;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.router.NodeRouter;

import java.util.List;

/**
 * 随机路由
 * @author liaochongwei
 * @date 2023/5/22 17:26
 */
public class RandomRouter implements NodeRouter {
    @Override
    public String route(Job job, List<String> addressList) {
        if (addressList.size() == 0) {
            return null;
        }
        return addressList.get(RandomUtil.randomInt(0, addressList.size()));
    }
}
