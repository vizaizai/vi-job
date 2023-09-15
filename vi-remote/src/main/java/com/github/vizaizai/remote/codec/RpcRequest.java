package com.github.vizaizai.remote.codec;

import com.github.vizaizai.remote.utils.Utils;

import java.beans.Transient;
import java.io.Serializable;

/**
 * RPC Request
 * @author liaochongwei
 * @date 2022/2/18 11:35
 */
public class RpcRequest implements Serializable {
    /**
     * 请求id
     */
    private transient String rid;
    /**
     * 业务码
     */
    private String bizCode;
    /**
     * 请求参数
     */
    private Object param;
    /**
     * 源id
     */
    private String originId;

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public static RpcRequest wrap(String bizCode, Object param) {
        RpcRequest request = new RpcRequest();
        request.setBizCode(bizCode);
        request.setParam(param);
        return request;
    }

    public static RpcRequest wrap(String bizCode, String originId, Object param) {
        RpcRequest request = new RpcRequest();
        request.setBizCode(bizCode);
        request.setOriginId(originId);
        request.setParam(param);
        return request;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    @Override
    public String toString() {
        return "{" +
                ", bizCode='" + bizCode + '\'' +
                ", param=" + param +
                '}';
    }
}
