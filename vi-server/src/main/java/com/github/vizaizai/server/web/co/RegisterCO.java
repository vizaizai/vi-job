package com.github.vizaizai.server.web.co;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 执行器注入
 * @author liaochongwei
 * @date 2023/5/8 14:19
 */
@Data
public class RegisterCO {
    /**
     * 应用名称
     */
    @NotNull(message = "应用名称必须")
    private String appName;
    /**
     * 注册地址
     */
    @NotNull(message = "注册地址必须")
    private String address;
}
