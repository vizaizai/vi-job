package com.github.vizaizai.common.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author liaochongwei
 * @date 2023/5/16 17:15
 */
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return this.code;
    }

    public Result<T> setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return this.msg;
    }

    public Result<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }


    public T getData() {
        return this.data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    public static <T> Result<T> ok() {
        return build(StatusCode.SUCCESS.getCode(), StatusCode.SUCCESS.getMsg());
    }

    public static <T> Result<T> ok(String msg) {
        return build(StatusCode.SUCCESS.getCode(), msg);
    }

    public static <T> Result<T> failure() {
        return build(StatusCode.FAILURE.getCode(), StatusCode.FAILURE.getMsg());
    }

    public static <T> Result<T> error() {
        return build(StatusCode.ERROR.getCode(), StatusCode.ERROR.getMsg());
    }

    public static <T> Result<T> unAuth() {
        return build(StatusCode.UN_AUTHORIZED.getCode(), StatusCode.UN_AUTHORIZED.getMsg());
    }

    public static <T> Result<T> non() {
        return build(StatusCode.NON.getCode(), StatusCode.NON.getMsg());
    }

    public static <T> Result<T> handleSuccess(T data) {
        Result<T> ret = ok();
        ret.data = data;
        return ret;
    }

    public static <T> Result<T> handleSuccess(String msg, T obj) {
        Result<T> ret = handleSuccess(obj);
        ret.setMsg(msg);
        return ret;
    }

    public static <T> Result<T> handleError(Exception e) {
        return error();
    }

    public static <T> Result<T> handleFailure(String msg) {
        Result<T> ret = failure();
        if (StringUtils.isNotBlank(msg)) {
            ret.setMsg(msg);
        }

        return ret;
    }

    public static <T> Result<T> handleNon(String msg) {
        Result<T> ret = non();
        if (StringUtils.isNotBlank(msg)) {
            ret.setMsg(msg);
        }

        return ret;
    }

    public static <T> Result<T> handleUnAuth(String msg) {
        Result<T> ret = unAuth();
        if (StringUtils.isNotBlank(msg)) {
            ret.setMsg(msg);
        }

        return ret;
    }

    private static <T> Result<T> build(Integer code, String msg) {
        Result<T> ret = new Result<>();
        ret.code  = code;
        ret.msg = msg;
        return ret;
    }

    public boolean isSuccess() {
        return Objects.equals(StatusCode.SUCCESS.getCode(), this.code);
    }
}