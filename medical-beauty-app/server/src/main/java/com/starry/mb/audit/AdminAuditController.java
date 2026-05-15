package com.starry.mb.audit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starry.mb.audit.domain.OperationLog;
import com.starry.mb.audit.dto.OperationLogVO;
import com.starry.mb.audit.mapper.OperationLogMapper;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.web.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditController {

    private static final Map<String, String> MODULE_LABELS = new HashMap<>() {{
        put("plan",        "规划方案");
        put("appointment", "预约");
        put("customer",    "客户");
        put("auth",        "认证");
        put("photo",       "照片");
    }};

    private static final Map<String, String> ACTION_LABELS = new HashMap<>() {{
        put("create",      "创建");
        put("update",      "更新");
        put("delete",      "删除");
        put("push",        "推送");
        put("book",        "下单");
        put("cancel",      "取消");
        put("admin_login", "登录后台");
        put("login",       "登录");
        put("logout",      "退出");
    }};

    private final OperationLogMapper logMapper;

    public AdminAuditController(OperationLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer operatorType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "1")  Integer page,
            @RequestParam(defaultValue = "30") Integer size) {

        Long tid = PrincipalContext.tenantId();
        LambdaQueryWrapper<OperationLog> q = new LambdaQueryWrapper<OperationLog>()
                .eq(OperationLog::getTenantId, tid)
                .orderByDesc(OperationLog::getCreatedAt);

        if (StringUtils.isNotBlank(module)) q.eq(OperationLog::getModule, module);
        if (StringUtils.isNotBlank(action)) q.eq(OperationLog::getAction, action);
        if (operatorType != null) q.eq(OperationLog::getOperatorType, operatorType);
        if (from != null) q.ge(OperationLog::getCreatedAt, from.atStartOfDay());
        if (to != null)   q.lt(OperationLog::getCreatedAt, to.plusDays(1).atStartOfDay());

        IPage<OperationLog> p = logMapper.selectPage(new Page<>(page, size), q);

        List<OperationLogVO> rows = p.getRecords().stream().map(l -> {
            OperationLogVO vo = new OperationLogVO();
            vo.setId(l.getId());
            vo.setOperatorType(l.getOperatorType());
            vo.setOperatorTypeLabel(switch (l.getOperatorType() == null ? 0 : l.getOperatorType()) {
                case 1 -> "员工"; case 2 -> "客户"; case 3 -> "系统"; default -> "未知"; });
            vo.setOperatorId(l.getOperatorId());
            vo.setOperatorName(l.getOperatorName());
            vo.setModule(l.getModule());
            vo.setModuleLabel(MODULE_LABELS.getOrDefault(l.getModule(), l.getModule()));
            vo.setAction(l.getAction());
            vo.setActionLabel(ACTION_LABELS.getOrDefault(l.getAction(), l.getAction()));
            vo.setTargetId(l.getTargetId());
            vo.setTargetSummary(l.getTargetSummary());
            vo.setIp(l.getIp());
            vo.setCreatedAt(l.getCreatedAt());
            return vo;
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("rows", rows);
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("size", size);
        return ApiResponse.ok(result);
    }
}
