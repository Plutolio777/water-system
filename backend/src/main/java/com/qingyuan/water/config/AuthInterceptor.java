package com.qingyuan.water.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.water.common.ApiResponse;
import io.jsonwebtoken.Claims;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) return true;
        String authorization = request.getHeader("Authorization");
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) throw new IllegalArgumentException();
            Claims claims = jwtUtil.parseToken(authorization.substring(7));
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("realName", claims.get("realName"));
            return true;
        } catch (Exception exception) {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(401, "登录已过期，请重新登录")));
            return false;
        }
    }
}
