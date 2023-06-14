package com.github.vizaizai.server.web.interceptor;

import com.github.vizaizai.server.constant.RoleType;
import com.github.vizaizai.server.dao.dataobject.UserDO;
import com.github.vizaizai.server.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;


@Slf4j
public class PermInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        HasRole hasRole = method.getAnnotation(HasRole.class);
        if (hasRole == null) {
            return true;
        }
        UserDO user = UserUtils.getUser();
        if (user != null && Objects.equals(hasRole.value(), RoleType.getValue(user.getRole()))) {
            return true;
        }
        throw new RuntimeException("您没有该功能的权限");
    }
}
