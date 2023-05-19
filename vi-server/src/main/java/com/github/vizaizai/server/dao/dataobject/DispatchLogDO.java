package com.github.vizaizai.server.dao.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调度日志
 * @author liaochongwei
 * @date 2023/5/18 19:51
 */
@Data
@TableName(value = "dispatch_log")
public class DispatchLogDO {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 任务id
     */
    private String jobId;

    /**
     * 执行器id
     */
    private String workerId;

    /**
     * 执行器地址
     */
    private String workerAddress;

    /**
     * 调度状态 0-失败 1-成功
     */
    private Integer dispatchStatus;

    /**
     * 执行状态 0-失败 1-执行中 2-执行成功 3-超时执行 4-超时中断 5-主动中断'
     */
    private Integer executeStatus;
    /**
     * 处理器类型 1-Bean 2-HTTP
     */
    private Integer processorType;
    /**
     * 处理器
     */
    private String processor;

    /**
     * 处理器参数
     */
    private String processorParam;

    /**
     * 触发时间
     */
    private LocalDateTime triggerTime;
    /**
     * 执行开始时间
     */
    private LocalDateTime executeStartTime;
    /**
     * 执行结束时间
     */
    private LocalDateTime executeEndTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
