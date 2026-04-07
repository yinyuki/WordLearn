package com.example.word.config;

import com.alibaba.fastjson.JSON;
import com.example.word.common.BaseContext;
import com.example.word.common.R;
import com.example.word.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        try {
            if (StringUtils.hasText(token)) {
                Long userId = JwtUtil.getUserIdFromToken(token);
                if (userId != null) {
                    BaseContext.setCurrentId(userId);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Token解析失败: {}", e.getMessage());
        }

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(R.error("NOT_LOGIN")));
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        BaseContext.removeCurrentId();
    }
}