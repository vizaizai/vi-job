package com.github.vizaizai.worker.core;

import com.github.vizaizai.common.model.StatusReportParam;
import com.github.vizaizai.common.model.TaskTriggerParam;
import com.github.vizaizai.remote.common.sender.Sender;
import com.github.vizaizai.worker.log.Logger;

/**
 * 任务上下文
 * @author liaochongwei
 * @date 2023/6/1 15:56
 */
public class TaskContext {
    /**
     * 任务触发参数
     */
    private final TaskTriggerParam triggerParam;
    /**
     * 消息发送者
     */
    private final Sender sender;
    /**
     * 状态上报参数
     */
    private StatusReportParam reportParam;
    /**
     * 日志记录器
     */
    private Logger logger;

    public TaskContext(TaskTriggerParam triggerParam, Sender sender) {
        this.triggerParam = triggerParam;
        this.sender = sender;
    }

    public TaskTriggerParam getTriggerParam() {
        return triggerParam;
    }

    public Sender getSender() {
        return sender;
    }

    public StatusReportParam getReportParam() {
        return reportParam;
    }

    public void setReportParam(StatusReportParam reportParam) {
        this.reportParam = reportParam;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
