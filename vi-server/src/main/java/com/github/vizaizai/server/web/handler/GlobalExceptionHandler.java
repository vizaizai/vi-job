package com.github.vizaizai.server.web.handler;

import com.github.vizaizai.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理
 *
 * @author liaochongwei
 * @date 2023/05/10
 * @since 1.8
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Autowired
    private Environment environment;

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result handlerParamMissException(MissingServletRequestParameterException e) {
        return Result.failure().setMsg("缺少参数【"+ e.getParameterName() +"】");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result HttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.info("参数映射错误:{}",e.getMessage());
        return Result.failure().setMsg("参数错误");
    }

    @ExceptionHandler(Exception.class)
    public Result handler(Exception ex) {
        String defaultMessage;
        if (ex instanceof BindException) {
            FieldError fieldError = ((BindException) ex).getFieldError();
            defaultMessage = this.getFieldMsg(fieldError);
        } else if (ex instanceof MethodArgumentNotValidException) {
            BindingResult bindingResult = ((MethodArgumentNotValidException) ex).getBindingResult();
            FieldError fieldError = bindingResult.getFieldError();
            defaultMessage = this.getFieldMsg(fieldError);
        } else if (ex instanceof MaxUploadSizeExceededException || ex instanceof FileUploadBase.SizeLimitExceededException) {
            defaultMessage = "文件大小超出2MB限制";
        }else if (ex instanceof RuntimeException) {
            defaultMessage = ex.getMessage();
        } else {
            defaultMessage = "操作失败";
        }
        log.error("请求发生异常=>", ex);
        return Result.handleFailure(defaultMessage);
    }


    private String getFieldMsg(FieldError error) {
        if (error == null) {
            return "参数错误";
        }
        String errMsg;
        // 如果绑定值失败
        if (error.isBindingFailure()) {
            errMsg = "参数错误";
            errMsg =  error.getRejectedValue() == null ? errMsg : errMsg + ":" + error.getRejectedValue();
        }else {
            errMsg = error.getDefaultMessage();
        }

        String env = environment.getProperty("spring.profiles.active", "");
        if ("dev".equals(env)) {
            return errMsg + "(" + error.getField() + ")";
        }
        return errMsg;
    }
}
