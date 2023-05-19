package com.github.vizaizai.server.constant;

/**
 * 触发类型
 * @author liaochongwei
 * @date 2023/5/19 15:02
 */
public enum TriggerType {
    NON(0,"非主动触发"),
    CRON(1,"cron"),
    SPEED(2,"固定频率"),
    DELAYED(3,"固定延时");
    private final int code;
    private final String msg;

    TriggerType(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static TriggerType getInstance(int code) {
        for (TriggerType triggerType : TriggerType.values()) {
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
