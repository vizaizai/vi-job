package com.github.vizaizai.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.server.web.co.LoginCO;
import com.github.vizaizai.server.web.co.UserAddCO;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.web.co.UserQueryCO;
import com.github.vizaizai.server.web.dto.UserDTO;
import com.github.vizaizai.server.web.dto.UserListDTO;

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
     * 分页查询系统用户
     * @param queryCO
     * @return
     */
    Result<IPage<UserListDTO>> page(UserQueryCO queryCO);

    /**
     * 删除用户
     * @param id
     * @return
     */
    Result<Void> remove(String id);
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

    /**
     * 检查token
     * @param token token
     * @return token
     */
    boolean checkToken(String token);

    /**
     * 登出
     * @return
     */
    Result<Void> logout();

}
