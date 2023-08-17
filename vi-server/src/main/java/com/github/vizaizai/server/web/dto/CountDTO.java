package com.github.vizaizai.server.web.dto;

import lombok.Data;

/**
 * 基础统计
 * @author liaochongwei
 * @date 2023/8/17 16:32
 */
@Data
public class CountDTO {
    /**
     * 总任务
     */
    private Long totalJobNum;
    /**
     * 运行中的任务实例数
     */
    private Long runningInstanceNum;
    /**
     * 执行器数量
     */
    private Long workerNum;
    /**
     * 执行器节点数
     */
    private Long workerNodeNum;
}
