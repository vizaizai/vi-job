package com.github.vizaizai.server.web.co;

import javax.validation.constraints.NotNull;

/**
 * 用户登录-CO
 * @author liaochongwei
 * @date 2023/5/6 11:40
 */
public class LoginCO {

    @NotNull(message = "用户名不能为空")
    private String userName;
    @NotNull(message = "密码不能为空")
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
