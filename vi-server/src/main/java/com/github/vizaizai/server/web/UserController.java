package com.github.vizaizai.server.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.server.web.co.IdCO;
import com.github.vizaizai.server.web.co.LoginCO;
import com.github.vizaizai.server.web.co.UserAddCO;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.web.co.UserQueryCO;
import com.github.vizaizai.server.web.dto.UserDTO;
import com.github.vizaizai.server.service.UserService;
import com.github.vizaizai.server.web.dto.UserListDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author liaochongwei
 * @date 2023/5/6 11:31
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/addSysUser")
    public Result<Void> addSysUser(@RequestBody @Validated UserAddCO addCO) {
        return userService.addSysUser(addCO);
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody @Validated LoginCO loginCO) {
        return userService.login(loginCO);
    }

    @GetMapping("/page")
    public Result<IPage<UserListDTO>> page(UserQueryCO queryCO) {
        return userService.page(queryCO);
    }

    @PostMapping("/remove")
    public Result<Void> remove(@RequestBody @Validated IdCO idCO) {
        return userService.remove(idCO.getId());
    }

    @GetMapping("/info")
    public Result<UserDTO> info() {
        return userService.info();
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }
}
