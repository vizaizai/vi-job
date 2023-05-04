package com.github.vizaizai.worker.sample.controller;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * @author liaochongwei
 * @date 2023/5/4 14:25
 */
public class UserInfoDTO {
    /**
     * 名称
     */
    private String name;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 描述
     */

    private String introduction;
    /**
     * 权限
     */
    private List<String> roles = Arrays.asList("admin");

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
