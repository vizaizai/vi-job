package com.github.vizaizai.server.dao.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import com.github.vizaizai.remote.utils.Utils;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 调度日志
 * @author liaochongwei
 * @date 2023/5/18 19:51
 */
@Data
@Accessors(chain = true)
@TableName(value = "dispatch_log")
public class DispatchLogDO {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 任务id
     */
    private Long jobId;
    /**
     * 任务参数
     */
    private String jobParam;

    /**
     * 执行器id
     */
    private Integer workerId;

    /**
     * 执行器地址
     */
    private String workerAddress;

    /**
     * 调度状态 0-失败 1-成功
     */
    private Integer dispatchStatus;

    /**
     * 执行状态 0-失败 1-执行中 2-执行成功 3-执行超时 4-取消
     */
    private Integer executeStatus;
    /**
     * 错误消息
     */
    private String errorMsg;
    /**
     * 处理器类型 1-Bean 2-HTTP
     */
    private Integer processorType;
    /**
     * 处理器
     */
    private String processor;
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
     * 执行次数
     */
    private Integer execCount;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 预计删除时间
     */
    private LocalDateTime expectedDeleteTime;

    public DispatchLogDO setErrorMsg(String errorMsg) {
        if (Utils.isNotBlank(errorMsg) && errorMsg.length() > 1024) {
            this.errorMsg = errorMsg.substring(0, 1023);
            return this;
        }
        this.errorMsg = errorMsg;
        return this;
    }
}
