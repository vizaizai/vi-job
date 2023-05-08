package com.github.vizaizai.server.service;

import com.github.vizaizai.server.web.co.LoginCO;
import com.github.vizaizai.server.web.co.UserAddCO;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.web.dto.UserDTO;

/**
 * 用户相关接口
 * @author liaochongwei
 * @date 2023/5/6 11:44
 */
public interface UserService {
    /**
     * 新增系统用户
     * @param userAddCO
     * @return
     */
    Result<Void> addSysUser(UserAddCO userAddCO);

    /**
     * 登录
     * @param loginCO
     * @return
     */
    Result<String> login(LoginCO loginCO);

    /**
     * 用户信息
     * @return
     */
    Result<UserDTO> info();

}
