package com.github.vizaizai.server.dao.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务信息
 * @author liaochongwei
 * @date 2023/5/18 17:11
 */
@Data
@TableName(value = "job")
public class JobDO {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 执行器id
     */
    private String workerId;

    /**
     * 生命周期开始
     */
    private LocalDateTime startTime;

    /**
     * 生命周期结束
     */
    private LocalDateTime endTime;

    /**
     * 任务状态 0-停止 1-运行中
     */
    private Integer status;
    /**
     * 处理器类型 1-Bean 2-HTTP
     */
    private Integer processorType;
    /**
     * 处理器
     */
    private String processor;

    /**
     * 任务参数
     */
    private String param;

    /**
     * 触发类型 0-非主动触发 1-cron 2-固定频率（秒）3-固定延时（秒）
     */
    private Integer triggerType;

    /**
     * cron表达式
     */
    private String cron;

    /**
     * 频率
     */
    private Integer speed;
    /**
     * 延时
     */
    private Integer delayed;

    /**
     * 下一次触发时间
     */
    private LocalDateTime nextTriggerTime;

    /**
     * 新建人
     */
    private String creater;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updater;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
