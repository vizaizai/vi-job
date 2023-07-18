package com.github.vizaizai.server.web.co;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 日志查询-CO
 * @author liaochongwei
 * @date 2023/5/10 16:29
 */
@Data
public class LogQueryCO {
    /**
     * 调度id
     */
    @NotNull(message = "数据id必须")
    private long id;
    /**
     * 日志起始位置
     */
    private Long startPos = 0L;
    /**
     * 日志返回最大行数
     */
    private Integer maxLines = 10;

}
