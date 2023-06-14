package com.github.vizaizai.server.web.co;

import lombok.Data;

/**
 * 任务信息查询-CO
 * @author liaochongwei
 * @date 2023/5/18 17:11
 */
@Data
public class JobQueryCO extends PageQueryCO {
    /**
     * 任务id
     */
    private Long id;
    /**
     * 任务名称
     */
    private String name;
    /**
     * 执行器id
     */
    private String workerId;


}
