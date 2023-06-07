package com.github.vizaizai.remote.codec;

import com.github.vizaizai.remote.utils.Utils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Rpc报文
 * @author liaochongwei
 * @date 2023/6/7 10:25
 */
public class RpcMessage implements Serializable {
    public static final String REQUEST = "request";

    public static final String RESPONSE = "response";
    /**
     * 追踪id
     */
    private String traceId;
    /**
     * 传输方向：request，response
     */
    private String direction;
    /**
     * 传输内容
     */
    private Object content;

    /**
     * 创建请求报文
     * @param request 请求
     * @return RpcMessage
     */
    public static RpcMessage createRequest(RpcRequest request) {
        RpcMessage message = new RpcMessage();
        message.traceId = Utils.getRequestId();
        message.direction = REQUEST;
        message.content = request;
        return message;
    }

    /**
     * 创建响应报文
     * @param traceId 追踪id
     * @param response 响应
     * @return RpcMessage
     */
    public static RpcMessage createResponse(String traceId, RpcResponse response) {
        RpcMessage message = new RpcMessage();
        message.traceId = traceId;
        message.direction = RESPONSE;
        message.content = response;
        return message;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getDirection() {
        return direction;
    }

    public RpcRequest getRequest() {
        if (!Objects.equals(this.getDirection(), RpcMessage.REQUEST)) {
            throw new RuntimeException("This message is not request");
        }
        RpcRequest request = (RpcRequest) content;
        request.setRid(this.traceId);
        return request;
    }

    public RpcResponse getResponse() {
        if (!Objects.equals(this.getDirection(), RpcMessage.RESPONSE)) {
            throw new RuntimeException("This message is not response");
        }
        return (RpcResponse) content;
    }

    @Override
    public String toString() {
        return "RpcMessage{" +
                "traceId='" + traceId + '\'' +
                ", direction='" + direction + '\'' +
                ", content=" + content +
                '}';
    }
}
