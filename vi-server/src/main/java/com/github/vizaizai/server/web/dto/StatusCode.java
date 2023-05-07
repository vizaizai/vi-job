package com.github.vizaizai.server.web.dto;

/**
 * @author liaochongwei
 * @date 2023/5/6 11:35
 */
public enum StatusCode {
    SUCCESS(200, "操作成功"),
    FAILURE(201, "操作失败"),
    DUP(202, "数据重复"),
    NON(204, "数据不存在"),
    NULL(301, "参数为空"),
    UN_AUTHORIZED(401, "请求未授权"),
    LOCATION(404, "路径错误"),
    ERROR(500, "系统繁忙"),
    DATABASE(501, "数据异常");

    private int code;
    private String msg;

    private StatusCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsg(int code) {
        StatusCode[] statusCodes = values();
        int count = statusCodes.length;

        for(int i = 0; i < count; ++i) {
            StatusCode c = statusCodes[i];
            if (c.getCode() == code) {
                return c.msg;
            }
        }

        return null;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
