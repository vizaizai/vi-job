package com.github.vizaizai.common.contants;

/**
 * 执行状态（扩展）
 * @author liaochongwei
 * @date 2023/5/19 15:02
 */
public enum ExtendExecStatus {
    UNKNOWN(0,"未知"),
    ING(1,"执行中"),
    WAIT(2,"待执行");
    private final int code;
    private final String msg;

    ExtendExecStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ExtendExecStatus getInstance(int code) {
        for (ExtendExecStatus triggerType : ExtendExecStatus.values()) {
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
