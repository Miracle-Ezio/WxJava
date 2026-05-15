package com.starry.mb.auth;

import com.starry.mb.auth.dto.AdminLoginRequest;
import com.starry.mb.auth.dto.AdminLoginResponse;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.web.ApiResponse;
import com.starry.mb.employee.domain.Employee;
import com.starry.mb.employee.mapper.EmployeeMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final EmployeeMapper employeeMapper;

    public AdminAuthController(AdminAuthService adminAuthService, EmployeeMapper employeeMapper) {
        this.adminAuthService = adminAuthService;
        this.employeeMapper = employeeMapper;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest req) {
        return ApiResponse.ok(adminAuthService.login(req));
    }

    /** 当前登录员工信息（前端刷新页面后用 token 调一下复活会话）。 */
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        Long id = PrincipalContext.employeeId();
        if (id == null) return ApiResponse.error(40100, "未登录");
        Employee e = employeeMapper.selectById(id);
        if (e == null) return ApiResponse.error(40100, "登录已失效");

        Map<String, Object> me = new HashMap<>();
        me.put("id", e.getId());
        me.put("name", e.getName());
        me.put("avatarUrl", e.getAvatarUrl());
        me.put("jobTitle", e.getJobTitle());
        me.put("roleCode", e.getRoleCode());
        me.put("storeId", e.getStoreId());
        return ApiResponse.ok(me);
    }
}
