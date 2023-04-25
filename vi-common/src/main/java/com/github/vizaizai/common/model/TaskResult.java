package com.github.vizaizai.common.model;

import java.io.Serializable;

/**
 * 任务结果
 * @author liaochongwei
 * @date 2023/4/23 10:38
 */
public class TaskResult implements Serializable {
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 错误消息
     */
    private String msg;

    private TaskResult() {
    }

    public static TaskResult ok() {
        TaskResult result = new TaskResult();
        result.success = true;
        result.msg = "ok";
        return result;
    }

    public static  TaskResult fail(String msg) {
        TaskResult result = new TaskResult();
        result.success = false;
        result.msg = msg;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "{" +
                "success=" + success +
                ", msg='" + msg + '\'' +
                '}';
    }
}
