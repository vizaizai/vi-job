package com.github.vizaizai.server.utils;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.RegisteredPayload;
import com.github.vizaizai.server.entity.User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户工具类
 * @author liaochongwei
 * @date 2023/5/6 15:53
 */
public class UserUtils {

    public static final String key = "o64nbbf2uryzj8z34acomrp81sjdzfnh7bqsblqv3k93w50fnc9oixnltefdcnib";

    public static User getUser() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("X-Token");
        JWT jwt = JWTUtil.parseToken(token);
        return jwt.getPayloads().toBean(User.class);
    }


    public static String createToken(User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", user.getId());
        payload.put("userName", user.getUserName());
        payload.put(RegisteredPayload.ISSUED_AT, new Date());
        payload.put(RegisteredPayload.NOT_BEFORE, new Date());
        payload.put(RegisteredPayload.EXPIRES_AT, new Date(new Date().getTime() + 1000 * 60 * 60 * 24) );
        return JWTUtil.createToken(payload, key.getBytes());
    }
}
