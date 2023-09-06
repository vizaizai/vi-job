package com.github.vizaizai.server.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.dao.TokenMapper;
import com.github.vizaizai.server.dao.UserMapper;
import com.github.vizaizai.server.dao.dataobject.TokenDO;
import com.github.vizaizai.server.dao.dataobject.UserDO;
import com.github.vizaizai.server.service.UserService;
import com.github.vizaizai.server.utils.BeanUtils;
import com.github.vizaizai.server.utils.UserUtils;
import com.github.vizaizai.server.web.co.LoginCO;
import com.github.vizaizai.server.web.co.UserAddCO;
import com.github.vizaizai.server.web.co.UserQueryCO;
import com.github.vizaizai.server.web.dto.UserDTO;
import com.github.vizaizai.server.web.dto.UserListDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Objects;

/**
 * @author liaochongwei
 * @date 2023/5/6 11:46
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private TokenMapper tokenMapper;

    @Transactional
    @Override
    public Result<Void> addSysUser(UserAddCO userAddCO) {
        UserDO user = BeanUtils.toBean(userAddCO, UserDO::new);
        UserDO userExist = userMapper.findByUserName(userAddCO.getUserName());
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
    public Result<IPage<UserListDTO>> page(UserQueryCO queryCO) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.<UserDO>lambdaQuery()
                .eq(queryCO.getUserName() != null, UserDO::getUserName, queryCO.getUserName())
                .orderByDesc(UserDO::getCreateTime);
        Page<UserDO> userPage = userMapper.selectPage(queryCO.toPage(), queryWrapper);
        return Result.handleSuccess(BeanUtils.toPageBean(userPage, UserListDTO::new));
    }

    @Transactional
    @Override
    public Result<Void> remove(String id) {
        if (Objects.equals(UserUtils.getUserId(), id)) {
            return Result.handleFailure("您不能删除自己");
        }
        userMapper.deleteById(id);
        return Result.ok();
    }

    @Override
    public Result<String> login(LoginCO loginCO) {
        UserDO user = userMapper.findByUserName(loginCO.getUserName());
        if (user == null) {
            log.info("用户名【{}】不存在",loginCO.getUserName());
            return Result.handleFailure("用户名或密码错误");
        }
        String inputPwd = DigestUtil.md5Hex(DigestUtil.sha1Hex(loginCO.getPassword())  + user.getPasswordSalt());
        if (!inputPwd.equals(user.getPassword())) {
            log.info("用户名【{}】,密码不匹配",loginCO.getUserName());
            return Result.handleFailure("用户名或密码错误");
        }
        String token = UserUtils.createToken(user);

        TokenDO tokenDO = new TokenDO();
        tokenDO.setUserId(user.getId());
        tokenDO.setToken(token);
        tokenDO.setTokenKey(DigestUtil.md5Hex(token));
        tokenDO.setExpireTime(LocalDateTimeUtil.now().plusDays(1));
        tokenMapper.insert(tokenDO);
        return Result.handleSuccess(token);
    }

    @Override
    public Result<UserDTO> info() {
        UserDO user = UserUtils.getUser();
        if (user != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setUserName(user.getUserName());
            userDTO.setRoles(Collections.singletonList(user.getRole()));
            return Result.handleSuccess(userDTO);
        }
        return Result.ok();

    }

    @Override
    public boolean checkToken(String token) {
        Long count = tokenMapper.selectCount(Wrappers.<TokenDO>lambdaQuery()
                .eq(TokenDO::getTokenKey, DigestUtil.md5Hex(token))
                .gt(TokenDO::getExpireTime, LocalDateTimeUtil.now()));
        return count != null && count > 0;
    }

    @Override
    public Result<Void> logout() {
        String token = UserUtils.getToken();
        if (token == null) {
            return Result.handleFailure("未授权");
        }
        tokenMapper.delete(Wrappers.<TokenDO>lambdaQuery()
                .eq(TokenDO::getTokenKey, DigestUtil.md5Hex(token)));
        return Result.ok();
    }
}
