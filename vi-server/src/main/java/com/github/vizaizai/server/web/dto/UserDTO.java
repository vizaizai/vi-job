package com.github.vizaizai.server.web.dto;

import java.util.Arrays;
import java.util.List;

/**
 * 系统用户
 * @author liaochongwei
 * @date 2023/5/6 11:13
 */
public class UserDTO {
    private String id;
    private String userName;
    private List<Integer> roles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<Integer> getRoles() {
        return roles;
    }

    public void setRoles(List<Integer> roles) {
        this.roles = roles;
    }
}
