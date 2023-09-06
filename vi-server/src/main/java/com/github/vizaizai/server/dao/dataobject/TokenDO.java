package com.github.vizaizai.server.dao.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 鉴权TOKEN
 * @author liaochongwei
 * @date 2023/9/6 10:23
 */
@Data
@TableName(value = "token")
public class TokenDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    private String token;

    private String tokenKey;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime expireTime;
}
