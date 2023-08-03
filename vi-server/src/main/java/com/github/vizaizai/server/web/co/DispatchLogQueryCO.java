package com.github.vizaizai.server.web.co;

import com.github.vizaizai.server.constant.Commons;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 调度日志查询-CO
 * @author liaochongwei
 * @date 2023/5/18 17:11
 */
@Data
public class DispatchLogQueryCO extends PageQueryCO {
    /**
     * 任务id
     */
    private Long jobId;
    /**
     * 执行器id
     */
    private Integer workerId;
    /**
     * 调度状态 0-失败 1-调度中 2-调度成功
     */
    private Integer dispatchStatus;

    /**
     * 执行状态 0-失败 1-执行中 2-执行成功 3-执行超时 4-取消
     */
    private Integer executeStatus;
    /**
     * 触发开始时间
     */
    @DateTimeFormat(pattern = Commons.DT_PATTERN)
    private LocalDateTime triggerStartTime;
    /**
     * 触发结束时间
     */
    @DateTimeFormat(pattern = Commons.DT_PATTERN)
    private LocalDateTime triggerEndTime;


}
