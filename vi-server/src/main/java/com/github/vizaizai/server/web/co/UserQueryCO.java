package com.github.vizaizai.server.web.co;

import lombok.Data;

/**
 * 执行器查询-CO
 * @author liaochongwei
 * @date 2023/5/18 17:11
 */
@Data
public class UserQueryCO extends PageQueryCO {
    /**
     * 用户名
     */
    private String userName;
}
