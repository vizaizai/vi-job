package com.github.vizaizai.server.dao.dataobject;

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
public class RegistryDO {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 执行器id
     */
    private Integer workerId;
    /**
     * 注册地址
     */
    private String address;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updateTime;
}
