package com.github.vizaizai.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行器
 * @author liaochongwei
 * @date 2023/5/7 17:10
 */
@Data
@TableName(value = "worker")
public class Worker {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 执行器名称
     */
    private String name;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 在线地址列表
     */
    private String addrList;

    private String creater;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updater;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;


}
