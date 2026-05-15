package com.starry.mb.common.bootstrap;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starry.mb.employee.domain.Employee;
import com.starry.mb.employee.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 演示数据引导：确保 V2 seed 的顾问账号有可登录的手机号 + 密码。
 *
 * 演示账号：
 *   手机：13800000001
 *   密码：starry123
 *
 * 生产环境应通过运营平台 / 管理员邀请流程下发账号，移除本类。
 */
@Slf4j
@Component
@Order(100)
public class EmployeeSeedBootstrap implements CommandLineRunner {

    private static final String DEMO_PHONE    = "13800000001";
    private static final String DEMO_PASSWORD = "starry123";

    private final EmployeeMapper employeeMapper;

    public EmployeeSeedBootstrap(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public void run(String... args) {
        Employee e = employeeMapper.selectOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getId, 1L)
                .last("LIMIT 1"));
        if (e == null) return;

        boolean changed = false;
        String phoneHash = DigestUtil.sha256Hex(DEMO_PHONE);
        if (!phoneHash.equals(e.getPhoneHash())) {
            e.setPhone(DEMO_PHONE);            // 注：生产应 AES 加密；演示阶段明文
            e.setPhoneHash(phoneHash);
            changed = true;
        }
        if (e.getPasswordHash() == null || e.getPasswordHash().isBlank()) {
            e.setPasswordHash(BCrypt.hashpw(DEMO_PASSWORD));
            changed = true;
        }
        if (changed) {
            employeeMapper.updateById(e);
            log.info("[EmployeeSeedBootstrap] demo employee credentials ready: {} / {}",
                    DEMO_PHONE, DEMO_PASSWORD);
        }
    }
}
