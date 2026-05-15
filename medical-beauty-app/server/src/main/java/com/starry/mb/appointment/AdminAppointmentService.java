package com.starry.mb.appointment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starry.mb.appointment.domain.Appointment;
import com.starry.mb.appointment.domain.AppointmentStatus;
import com.starry.mb.appointment.dto.AppointmentVO;
import com.starry.mb.appointment.mapper.AppointmentMapper;
import com.starry.mb.audit.AuditLogger;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.exception.BizException;
import com.starry.mb.customer.domain.Customer;
import com.starry.mb.customer.mapper.CustomerMapper;
import com.starry.mb.notify.WxSubscribeService;
import com.starry.mb.store.domain.Store;
import com.starry.mb.store.mapper.StoreMapper;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class AdminAppointmentService {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("MM 月 dd 日 HH:mm");

    private final AppointmentMapper appointmentMapper;
    private final CustomerMapper customerMapper;
    private final StoreMapper storeMapper;
    private final WxSubscribeService wxSubscribe;
    private final AuditLogger audit;

    public AdminAppointmentService(AppointmentMapper appointmentMapper,
                                   CustomerMapper customerMapper,
                                   StoreMapper storeMapper,
                                   WxSubscribeService wxSubscribe,
                                   AuditLogger audit) {
        this.appointmentMapper = appointmentMapper;
        this.customerMapper = customerMapper;
        this.storeMapper = storeMapper;
        this.wxSubscribe = wxSubscribe;
        this.audit = audit;
    }

    @Data
    public static class ListResult {
        private List<AppointmentVO> rows;
        private long total;
    }

    public ListResult list(Integer status, LocalDate dateFrom, LocalDate dateTo,
                           String keyword, int page, int size) {
        Long tid = PrincipalContext.tenantId();
        LambdaQueryWrapper<Appointment> q = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getTenantId, tid)
                .orderByDesc(Appointment::getStartAt);
        if (status != null) q.eq(Appointment::getStatus, status);
        if (dateFrom != null) q.ge(Appointment::getStartAt, dateFrom.atStartOfDay());
        if (dateTo != null)   q.lt(Appointment::getStartAt, dateTo.plusDays(1).atStartOfDay());

        if (StringUtils.isNotBlank(keyword)) {
            List<Long> matchedCustomerIds = customerMapper.selectList(new LambdaQueryWrapper<Customer>()
                    .eq(Customer::getTenantId, tid)
                    .and(w -> w.like(Customer::getNickname, keyword)
                            .or().like(Customer::getRealName, keyword)))
                    .stream().map(Customer::getId).toList();
            if (matchedCustomerIds.isEmpty()) {
                ListResult empty = new ListResult();
                empty.setRows(List.of());
                empty.setTotal(0);
                return empty;
            }
            q.in(Appointment::getCustomerId, matchedCustomerIds);
        }

        Page<Appointment> p = appointmentMapper.selectPage(new Page<>(page, size), q);
        ListResult r = new ListResult();
        r.setRows(p.getRecords().stream().map(this::toVO).toList());
        r.setTotal(p.getTotal());
        return r;
    }

    @Transactional
    public AppointmentVO confirm(long id, String staffNote) {
        Appointment a = mustExist(id);
        ensureStatus(a, AppointmentStatus.PENDING.getCode());
        a.setStatus(AppointmentStatus.CONFIRMED.getCode());
        a.setConfirmedAt(LocalDateTime.now());
        if (staffNote != null) a.setStaffNote(staffNote);
        appointmentMapper.updateById(a);

        wxSubscribe.notifyAppointmentConfirmed(a.getId(), a.getCustomerId(),
                a.getProjectName(), a.getStartAt().format(WHEN), storeName(a.getStoreId()));
        audit.record("appointment", "confirm", id, "确认预约：" + a.getProjectName());
        return toVO(a);
    }

    @Transactional
    public AppointmentVO checkIn(long id) {
        Appointment a = mustExist(id);
        ensureStatus(a, AppointmentStatus.CONFIRMED.getCode());
        a.setStatus(AppointmentStatus.CHECKED_IN.getCode());
        a.setCheckedInAt(LocalDateTime.now());
        appointmentMapper.updateById(a);
        audit.record("appointment", "check_in", id, "客户到店：" + a.getProjectName());
        return toVO(a);
    }

    @Transactional
    public AppointmentVO complete(long id, String staffNote) {
        Appointment a = mustExist(id);
        ensureStatus(a, AppointmentStatus.CHECKED_IN.getCode());
        a.setStatus(AppointmentStatus.COMPLETED.getCode());
        a.setCompletedAt(LocalDateTime.now());
        if (staffNote != null) a.setStaffNote(staffNote);
        appointmentMapper.updateById(a);
        audit.record("appointment", "complete", id, "完成预约：" + a.getProjectName());
        return toVO(a);
    }

    @Transactional
    public AppointmentVO cancelByBiz(long id, String reason) {
        Appointment a = mustExist(id);
        if (a.getStatus() >= AppointmentStatus.COMPLETED.getCode()) {
            throw new BizException(40010, "当前状态不可取消");
        }
        a.setStatus(AppointmentStatus.CANCELLED_BY_BIZ.getCode());
        a.setCancelledAt(LocalDateTime.now());
        a.setCancelReason(reason);
        appointmentMapper.updateById(a);
        audit.record("appointment", "cancel_by_biz", id,
                "机构取消预约：" + a.getProjectName() + (reason == null ? "" : " · " + reason));
        return toVO(a);
    }

    @Transactional
    public AppointmentVO markNoShow(long id) {
        Appointment a = mustExist(id);
        if (!AppointmentStatus.ACTIVE.contains(a.getStatus())) {
            throw new BizException(40010, "当前状态不可标记爽约");
        }
        a.setStatus(AppointmentStatus.NO_SHOW.getCode());
        a.setCancelledAt(LocalDateTime.now());
        appointmentMapper.updateById(a);
        audit.record("appointment", "no_show", id, "标记爽约：" + a.getProjectName());
        return toVO(a);
    }

    @Transactional
    public AppointmentVO updateStaffNote(long id, String staffNote) {
        Appointment a = mustExist(id);
        a.setStaffNote(staffNote);
        appointmentMapper.updateById(a);
        audit.record("appointment", "update_note", id, "更新员工备注");
        return toVO(a);
    }

    // ─────────────────────────────────────────────────────────

    private Appointment mustExist(long id) {
        Long tid = PrincipalContext.tenantId();
        Appointment a = appointmentMapper.selectById(id);
        if (a == null || a.getDeletedAt() != null || !a.getTenantId().equals(tid)) {
            throw new BizException(40400, "预约不存在");
        }
        return a;
    }

    private void ensureStatus(Appointment a, int expected) {
        if (a.getStatus() == null || a.getStatus() != expected) {
            throw new BizException(40010,
                    "当前状态为 " + AppointmentStatus.ofCode(a.getStatus()).getLabel()
                    + "，无法执行此操作");
        }
    }

    private String storeName(Long storeId) {
        if (storeId == null) return "";
        Store s = storeMapper.selectById(storeId);
        return s == null ? "" : s.getName();
    }

    private AppointmentVO toVO(Appointment a) {
        AppointmentVO vo = new AppointmentVO();
        vo.setId(a.getId());
        vo.setStoreId(a.getStoreId());
        vo.setProjectId(a.getProjectId());
        vo.setProjectName(a.getProjectName());
        vo.setUnitPrice(a.getUnitPrice());
        vo.setDurationMin(a.getDurationMin());
        vo.setStartAt(a.getStartAt());
        vo.setEndAt(a.getEndAt());
        vo.setStatus(a.getStatus());
        vo.setStatusLabel(AppointmentStatus.ofCode(a.getStatus()).getLabel());
        vo.setSource(a.getSource());
        vo.setCustomerNote(a.getCustomerNote());
        vo.setCancelReason(a.getCancelReason());
        if (a.getStoreId() != null) {
            Store s = storeMapper.selectById(a.getStoreId());
            if (s != null) vo.setStoreName(s.getName());
        }
        if (a.getCustomerId() != null) {
            Customer c = customerMapper.selectById(a.getCustomerId());
            if (c != null) vo.setCustomerName(c.getRealName() != null ? c.getRealName() : c.getNickname());
        }
        return vo;
    }
}
