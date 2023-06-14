package com.github.vizaizai.server.web.co;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 任务运行-CO
 * @author liaochongwei
 * @date 2023/5/18 17:11
 */
@Data
public class JobRunCO {
    /**
     * 数据id
     */
    @NotNull(message = "数据id")
    private Long id;
    /**
     * 任务参数
     */
    private String jobParam;
}
