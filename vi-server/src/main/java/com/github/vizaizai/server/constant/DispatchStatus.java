package com.github.vizaizai.server.constant;

/**
 * 调度状态
 * @author liaochongwei
 * @date 2023/5/19 15:02
 */
public enum DispatchStatus {
    FAIL(0,"失败"),
    OK(1,"成功");
    private final int code;
    private final String msg;

    DispatchStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static DispatchStatus getInstance(int code) {
        for (DispatchStatus triggerType : DispatchStatus.values()) {
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
