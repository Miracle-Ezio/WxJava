package com.starry.mb.auth;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starry.mb.audit.AuditLogger;
import com.starry.mb.auth.dto.AdminLoginRequest;
import com.starry.mb.auth.dto.AdminLoginResponse;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.exception.BizException;
import com.starry.mb.common.security.JwtUtil;
import com.starry.mb.employee.domain.Employee;
import com.starry.mb.employee.mapper.EmployeeMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final EmployeeMapper employeeMapper;
    private final JwtUtil jwtUtil;
    private final AuditLogger audit;
    private final Long defaultTenantId;

    public AdminAuthService(EmployeeMapper employeeMapper,
                            JwtUtil jwtUtil,
                            AuditLogger audit,
                            @Value("${starry.demo.default-tenant-id}") Long defaultTenantId) {
        this.employeeMapper = employeeMapper;
        this.jwtUtil = jwtUtil;
        this.audit = audit;
        this.defaultTenantId = defaultTenantId;
    }

    public AdminLoginResponse login(AdminLoginRequest req) {
        String phoneHash = DigestUtil.sha256Hex(req.getPhone());
        Employee e = employeeMapper.selectOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getTenantId, defaultTenantId)
                .eq(Employee::getPhoneHash, phoneHash)
                .last("LIMIT 1"));
        if (e == null || e.getDeletedAt() != null || e.getStatus() == null || e.getStatus() != 1) {
            throw new BizException(40001, "手机号或密码错误");
        }
        if (e.getPasswordHash() == null || !BCrypt.checkpw(req.getPassword(), e.getPasswordHash())) {
            throw new BizException(40001, "手机号或密码错误");
        }

        PrincipalContext.Principal principal = new PrincipalContext.Principal(
                e.getId(),
                PrincipalContext.Type.EMPLOYEE,
                e.getTenantId(),
                e.getStoreId());
        String token = jwtUtil.issue(principal);

        // 登录时还没设上下文，先手动设一下让 audit 拿到信息
        PrincipalContext.set(principal);
        try {
            audit.record("auth", "admin_login", e.getId(), "登录后台：" + e.getName());
        } finally {
            // 拦截器后续会再次写入 / 清理，这里不主动清以免影响响应链
        }

        return new AdminLoginResponse(
                token, e.getId(), e.getName(), e.getAvatarUrl(),
                e.getJobTitle(), e.getRoleCode(), e.getStoreId());
    }
}
