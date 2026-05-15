package com.starry.mb.audit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starry.mb.audit.domain.OperationLog;
import com.starry.mb.audit.mapper.OperationLogMapper;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.customer.domain.Customer;
import com.starry.mb.customer.mapper.CustomerMapper;
import com.starry.mb.employee.domain.Employee;
import com.starry.mb.employee.mapper.EmployeeMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 显式调用的审计记录器。
 * 业务 Service 在关键动作处调 {@link #record} 即可。
 *
 * 不走 AOP 的原因：摘要 / before-after 需要业务上下文，AOP 拿不到。
 */
@Slf4j
@Component
public class AuditLogger {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final OperationLogMapper logMapper;
    private final EmployeeMapper employeeMapper;
    private final CustomerMapper customerMapper;

    public AuditLogger(OperationLogMapper logMapper,
                       EmployeeMapper employeeMapper,
                       CustomerMapper customerMapper) {
        this.logMapper = logMapper;
        this.employeeMapper = employeeMapper;
        this.customerMapper = customerMapper;
    }

    public void record(String module, String action, Long targetId, String summary) {
        record(module, action, targetId, summary, null, null);
    }

    public void record(String module, String action, Long targetId, String summary,
                       Object before, Object after) {
        try {
            OperationLog log = new OperationLog();
            PrincipalContext.Principal p = PrincipalContext.get();
            if (p != null) {
                log.setTenantId(p.getTenantId());
                log.setStoreId(p.getStoreId());
                if (p.getType() == PrincipalContext.Type.EMPLOYEE) {
                    log.setOperatorType(1);
                    log.setOperatorId(p.getId());
                    Employee e = lookupEmp(p.getId());
                    log.setOperatorName(e != null ? e.getName() : null);
                } else if (p.getType() == PrincipalContext.Type.CUSTOMER) {
                    log.setOperatorType(2);
                    log.setOperatorId(p.getId());
                    Customer c = customerMapper.selectById(p.getId());
                    log.setOperatorName(c != null
                            ? (c.getNickname() != null ? c.getNickname() : c.getRealName())
                            : null);
                }
            } else {
                log.setOperatorType(3);
                log.setOperatorName("system");
            }

            log.setModule(module);
            log.setAction(action);
            log.setTargetId(targetId);
            log.setTargetSummary(summary);
            if (before != null) log.setBeforeJson(JSON.writeValueAsString(before));
            if (after  != null) log.setAfterJson( JSON.writeValueAsString(after));

            HttpServletRequest req = currentRequest();
            if (req != null) {
                log.setIp(clientIp(req));
                log.setUserAgent(req.getHeader("User-Agent"));
            }
            logMapper.insert(log);
        } catch (Exception e) {
            AuditLogger.log.warn("audit record failed: module={} action={}", module, action, e);
        }
    }

    private Employee lookupEmp(Long id) {
        return employeeMapper.selectOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getId, id).last("LIMIT 1"));
    }

    private static HttpServletRequest currentRequest() {
        try {
            ServletRequestAttributes attr =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attr == null ? null : attr.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private static String clientIp(HttpServletRequest req) {
        String h = req.getHeader("X-Forwarded-For");
        if (h != null && !h.isBlank()) return h.split(",")[0].trim();
        h = req.getHeader("X-Real-IP");
        if (h != null && !h.isBlank()) return h;
        return req.getRemoteAddr();
    }
}
