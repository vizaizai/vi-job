package com.github.vizaizai.server.constant;

/**
 * 任务状态
 * @author liaochongwei
 * @date 2023/5/19 15:02
 */
public enum JobStatus {
    STOP(0,"停止"),
    RUN(1,"运行中");
    private final int code;
    private final String msg;

    JobStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static JobStatus getInstance(int code) {
        for (JobStatus triggerType : JobStatus.values()) {
            if (triggerType.getCode() == code) {
                return triggerType;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
