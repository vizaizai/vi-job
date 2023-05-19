package com.github.vizaizai.server.dao.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户
 * @author liaochongwei
 * @date 2023/5/6 11:13
 */
@Data
@TableName(value = "sys_user")
public class UserDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String userName;

    private String password;

    private String passwordSalt;

    private String creater;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updater;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
