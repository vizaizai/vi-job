package com.github.vizaizai.server.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.vizaizai.server.constant.Commons;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户
 * @author liaochongwei
 * @date 2023/5/6 11:13
 */
@Data
public class UserListDTO {

    private String id;

    private String userName;

    @JsonFormat(pattern = Commons.DT_PATTERN)
    private LocalDateTime createTime;

    /**
     * 角色 1-管理员 2-普通用户
     */
    private Integer role;
}
