package com.github.vizaizai.worker.sample.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liaochongwei
 * @date 2023/4/28 16:02
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @PostMapping("/login")
    public Object login(@RequestBody Map<String,Object> params) {
        HashMap<Object, Object> token = new HashMap<>();
        token.put("token", "11111111111111111111111111");
        return new Result<>(20000,"成功",token);
    }

    @GetMapping("/info")
    public Object login(@RequestParam String token) {
        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setName("admin");
        return new Result<>(20000,"成功",userInfo);
    }

}
