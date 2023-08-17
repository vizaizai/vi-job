package com.github.vizaizai.server.web.dto;

import lombok.Data;

/**
 * 服务节点-DTO
 * @author liaochongwei
 * @date 2023/5/18 17:11
 */
@Data
public class ServerNodeDTO {
    /**
     * 地址
     */
    private String address;
    /**
     * 状态
     */
    private Integer state;
    /**
     * 角色
     */
    private String role;
    /**
     * 元数据
     */
    private String mateData;
}
