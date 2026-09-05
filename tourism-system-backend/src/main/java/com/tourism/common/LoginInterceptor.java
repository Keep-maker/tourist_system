package com.tourism.common;

import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 * 拦截全部接口，只放行登录接口 /api/admin/login，其余全部校验token
 * 
 * 拦截规则:
 * - 放行: /api/admin/login (登录接口)
 * - 放行: 静态资源 (.html, .js, .css等)
 * - 其余所有 /api/** 接口均需校验token
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenUtil tokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行(CORS预检请求)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        // 登录接口和注册接口放行
        if (uri.equals("/api/admin/login") || uri.equals("/api/admin/register")) {
            return true;
        }

        // 获取请求头中的token
        String token = request.getHeader("Authorization");
        
        // 如果没有token，尝试从参数获取
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }

        // 校验token
        if (token == null || token.isEmpty() || !tokenUtil.validateToken(token)) {
            // 返回401未登录
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(JSON.toJSONString(Result.unauthorized()));
            return false;
        }

        // 校验通过，将管理员ID存入request属性
        Integer adminId = tokenUtil.getAdminIdByToken(token);
        request.setAttribute("adminId", adminId);
        request.setAttribute("token", token);

        return true;
    }
}
