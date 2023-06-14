package com.github.vizaizai.server.constant;

/**
 * 角色类型
 * @author liaochongwei
 * @date 2023/5/19 15:02
 */
public enum RoleType {
    ADMIN(1,"admin"),
    NORMAL_USER(2,"normal");
    private final int code;
    private final String value;

    RoleType(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public static RoleType getInstance(int code) {
        for (RoleType triggerType : RoleType.values()) {
            if (triggerType.getCode() == code) {
                return triggerType;
            }
        }
        return null;
    }

    public static String getValue(int code) {
        for (RoleType triggerType : RoleType.values()) {
            if (triggerType.getCode() == code) {
                return triggerType.value;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
