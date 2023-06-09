package com.github.vizaizai.server.web.co;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户添加-CO
 * @author liaochongwei
 * @date 2023/5/6 11:40
 */
@Data
public class UserAddCO {

    @NotNull(message = "用户名不能为空")
    @Pattern(regexp = "[a-zA-Z0-9\\s]{5,20}",message = "用户名格式不正确,请输入5-20个以内的英文或数字")
    private String userName;
    @NotNull(message = "初始密码不能为空")
    @Size(min = 6, max = 18, message = "请输入6~18位的字符")
    private String password;

    @NotNull(message = "请选择角色")
    private Integer role;
}
