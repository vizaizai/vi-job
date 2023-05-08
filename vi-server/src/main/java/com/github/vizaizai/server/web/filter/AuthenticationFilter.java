package com.github.vizaizai.server.web.filter;

import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.github.vizaizai.server.utils.UserUtils;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.common.model.StatusCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

@Component
@WebFilter(filterName = "authenticationFilter", urlPatterns = "/*")
public class AuthenticationFilter implements Filter {


    @Value("#{'${auth.excluded}'.split(',')}")
    private String[] exclude;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        if (StringUtils.startsWith(requestURI, contextPath)) {
            requestURI = StringUtils.replace(requestURI, contextPath, "");
        }

        boolean excludeFlag = false;
        String path;
        if (this.exclude != null && this.exclude.length > 0) {
            String[] paths = this.exclude;
            for (String s : paths) {
                path = s;
                if (StringUtils.endsWith(path, "/**")) {
                    if (StringUtils.startsWith(requestURI, StringUtils.substring(path, 0, path.length() - 3))) {
                        excludeFlag = true;
                        break;
                    }
                } else if (StringUtils.endsWith(path, "/*")) {
                    if (StringUtils.startsWith(requestURI, StringUtils.substring(path, 0, path.length() - 2))) {
                        excludeFlag = true;
                        break;
                    }
                } else if (StringUtils.equals(requestURI, path)) {
                    excludeFlag = true;
                    break;
                }
            }
        }

        if (excludeFlag) {
            chain.doFilter(request, response);
            return;
        }else {
            String token = request.getHeader("X-Token");
            if (StringUtils.isBlank(token)) {
                this.writeUnAuth(response, "未授权");
                return;
            }
            JWT jwt = JWTUtil.parseToken(token);
            jwt.setKey(UserUtils.key.getBytes());
            // token已失效
            if (!jwt.verify() || !jwt.validate(60)) {
                this.writeUnAuth(response, "授权已失效");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void writeUnAuth(HttpServletResponse response,String msg) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setStatus(StatusCode.SUCCESS.getCode());
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().println(JSONUtil.toJsonStr(Result.handleUnAuth(msg)));
    }
}
