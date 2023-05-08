package com.github.vizaizai.server.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.github.vizaizai.server.dao.UserMapper;
import com.github.vizaizai.server.entity.User;
import com.github.vizaizai.server.service.UserService;
import com.github.vizaizai.server.utils.BeanUtils;
import com.github.vizaizai.server.utils.UserUtils;
import com.github.vizaizai.server.web.co.LoginCO;
import com.github.vizaizai.server.web.co.UserAddCO;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.web.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author liaochongwei
 * @date 2023/5/6 11:46
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Transactional
    @Override
    public Result<Void> addSysUser(UserAddCO userAddCO) {
        User user = BeanUtils.toBean(userAddCO, User::new);
        User userExist = userMapper.findByUserName(userAddCO.getUserName());
        if (userExist != null) {
            return Result.handleFailure("用户已存在");
        }
        user.setPasswordSalt(RandomUtil.randomString(5));
        user.setPassword(DigestUtil.md5Hex(DigestUtil.sha1Hex(user.getPassword())  + user.getPasswordSalt()));
        user.setCreater(UserUtils.getUserName());
        userMapper.insert(user);
        return Result.ok("新增用户成功");
    }

    @Override
    public Result<String> login(LoginCO loginCO) {
        User user = userMapper.findByUserName(loginCO.getUserName());
        if (user == null) {
            log.info("用户名【{}】不存在",loginCO.getUserName());
            return Result.handleFailure("用户名或密码错误");
        }
        String inputPwd = DigestUtil.md5Hex(DigestUtil.sha1Hex(loginCO.getPassword())  + user.getPasswordSalt());
        if (!inputPwd.equals(user.getPassword())) {
            log.info("用户名【{}】,密码不匹配",loginCO.getUserName());
            return Result.handleFailure("用户名或密码错误");
        }
        return Result.handleSuccess(UserUtils.createToken(user));
    }

    @Override
    public Result<UserDTO> info() {
        User user = UserUtils.getUser();
        return Result.handleSuccess(BeanUtils.toBean(user, UserDTO::new));
    }
}
