package com.github.vizaizai.server.web.co;

import lombok.Data;

/**
 * 执行器查询-CO
 * @author liaochongwei
 * @date 2023/5/18 17:11
 */
@Data
public class WorkerQueryCO extends PageQueryCO {
    /**
     * 应用名称
     */
    private String appName;
}
