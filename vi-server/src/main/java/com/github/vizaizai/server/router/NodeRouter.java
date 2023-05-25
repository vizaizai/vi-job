package com.github.vizaizai.server.router;

import com.github.vizaizai.common.model.TaskContext;
import com.github.vizaizai.server.entity.Job;

import java.util.List;

/**
 * 执行器节点路由
 * @author liaochongwei
 * @date 2023/5/22 17:23
 */
public interface NodeRouter {

    String route(Job job, List<String> addressList);
}
