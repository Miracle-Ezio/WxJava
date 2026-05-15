package com.starry.mb.common.config;

import com.starry.mb.common.security.AdminScopeInterceptor;
import com.starry.mb.common.security.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminScopeInterceptor adminScopeInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor,
                        AdminScopeInterceptor adminScopeInterceptor) {
        this.authInterceptor = authInterceptor;
        this.adminScopeInterceptor = adminScopeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1) 通用 JWT 解析，所有 /api/** 默认要求登录
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/admin/auth/login",
                        "/api/public/**",
                        "/actuator/**"
                );

        // 2) /api/admin/** 必须为 EMPLOYEE 主体；客户的 token 进不来
        registry.addInterceptor(adminScopeInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
