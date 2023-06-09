package com.github.vizaizai.server.web.dto;

import lombok.Data;

/**
 * 注册表
 * @author liaochongwei
 * @date 2023/5/7 17:53
 */
@Data
public class RegistryDTO {

    private Integer id;
    /**
     * 执行器id
     */
    private Integer workerId;
    /**
     * 注册地址
     */
    private String address;
    /**
     * 在线状态 1-是 0-否
     */
    private Integer online;
}
