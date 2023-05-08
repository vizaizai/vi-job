package com.github.vizaizai.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 注册表
 * @author liaochongwei
 * @date 2023/5/7 17:53
 */
@Data
@TableName(value = "registry")
public class Registry {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 注册地址
     */
    private String address;
    /**
     * 是否在线 1-是 0-否
     */
    private Integer online;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
