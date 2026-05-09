package com.library.backend.config;

import com.library.backend.annotation.RequireRole;
import com.library.backend.exception.AccessDeniedException;
import com.library.backend.mapper.UserMapper;
import com.library.backend.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        if (requireRole != null) {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                throw new AccessDeniedException("User not authenticated");
            }

            List<String> userRoles = userMapper.getRoleCodesByUserId(userId);
            String[] requiredRoles = requireRole.value();
            
            boolean hasRole = false;
            for (String role : requiredRoles) {
                if (userRoles.contains(role)) {
                    hasRole = true;
                    break;
                }
            }

            if (!hasRole) {
                throw new AccessDeniedException("Access denied: Insufficient permissions");
            }
        }

        return true;
    }
}
