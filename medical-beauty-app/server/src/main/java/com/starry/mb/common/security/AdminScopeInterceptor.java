package com.starry.mb.common.security;

import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * /api/admin/** 路径放行后，仍需保证当前主体为 EMPLOYEE 类型。
 * 客户的 token 可以拿到 /api/photos/...，但拿不到 /api/admin/...。
 */
@Component
public class AdminScopeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        PrincipalContext.Principal p = PrincipalContext.get();
        if (p == null || p.getType() != PrincipalContext.Type.EMPLOYEE) {
            throw new BizException(40300, "仅员工可访问");
        }
        return true;
    }
}
