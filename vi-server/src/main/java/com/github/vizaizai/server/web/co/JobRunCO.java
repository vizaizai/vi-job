package com.github.vizaizai.server.web.co;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.vizaizai.server.constant.Commons;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

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
    private Long id;
    /**
     * 任务编码
     */
    private String jobCode;
    /**
     * 任务参数
     */
    private String jobParam;
    /**
     * 触发时间
     */
    @NotNull(message = "触发时间不能为空")
    @JsonFormat(pattern = Commons.DT_PATTERN)
    private LocalDateTime triggerTime;

}
