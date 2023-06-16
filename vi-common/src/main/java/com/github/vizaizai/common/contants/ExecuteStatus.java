package com.github.vizaizai.common.contants;

/**
 * 执行状态
 * @author liaochongwei
 * @date 2023/5/19 15:02
 */
public enum ExecuteStatus {
    FAIL(0,"执行失败"),
    ING(1,"执行中"),
    OK(2,"执行成功"),
    OK_TIMEOUT(3,"执行成功（超时）"),
    OFF_TIMEOUT(4,"超时中断"),
    OFF(5,"主动中断");
    private final int code;
    private final String msg;

    ExecuteStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ExecuteStatus getInstance(int code) {
        for (ExecuteStatus triggerType : ExecuteStatus.values()) {
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
