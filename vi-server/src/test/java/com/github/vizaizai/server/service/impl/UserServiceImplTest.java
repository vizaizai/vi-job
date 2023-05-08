package com.github.vizaizai.server.service.impl;


import com.github.vizaizai.server.web.co.LoginCO;
import com.github.vizaizai.server.web.co.UserAddCO;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceImplTest {

    @Resource
    private UserService userService;

    @Test
    public void addSysUser() {
        UserAddCO addCO = new UserAddCO();
        addCO.setPassword("123456");
        addCO.setUserName("admin");
        userService.addSysUser(addCO);
    }

    @Test
    public void login() {
        LoginCO loginCO = new LoginCO();
        loginCO.setPassword("123456");
        loginCO.setUserName("admin");
        Result<String> result = userService.login(loginCO);
        Assert.assertEquals("200", result.getCode() + "");
    }
}