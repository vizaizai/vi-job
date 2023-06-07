package com.github.vizaizai.remote.codec;

import java.io.Serializable;

/**
 * RPC Response
 * @author liaochongwei
 * @date 2022/2/18 11:40
 */
public class RpcResponse implements Serializable {
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 错误消息
     */
    private String msg = "";
    /**
     * 返回结果
     */
    private Object result;

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public static RpcResponse ok(Object result) {
        RpcResponse response = new RpcResponse();
        response.setSuccess(true);
        response.setResult(result);
        return response;
    }

    public static RpcResponse error(String msg) {
        RpcResponse response = new RpcResponse();
        response.setSuccess(false);
        response.setMsg(msg);
        return response;
    }

    @Override
    public String toString() {
        return "{" +
                ", success=" + success +
                ", msg='" + msg + '\'' +
                ", result=" + result +
                '}';
    }
}
