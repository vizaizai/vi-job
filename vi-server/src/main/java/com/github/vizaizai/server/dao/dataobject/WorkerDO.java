package com.github.vizaizai.server.dao.dataobject;

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
public class WorkerDO {

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

    private String creater;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updater;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;


}
