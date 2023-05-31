package com.github.vizaizai.server.web.co;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 任务状态更新-CO
 * @author liaochongwei
 * @date 2023/5/18 17:11
 */
@Data
public class JobStatusUpdateCO {
    /**
     * 数据id
     */
    @NotNull(message = "数据id")
    private Long id;
    /**
     * 0-停止 1-运行中
     */
    @NotNull(message = "任务状态必须")
    private Integer status;
}
