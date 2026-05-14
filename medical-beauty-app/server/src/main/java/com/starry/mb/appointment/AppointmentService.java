package com.starry.mb.appointment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starry.mb.appointment.domain.Appointment;
import com.starry.mb.appointment.domain.AppointmentStatus;
import com.starry.mb.appointment.dto.AppointmentVO;
import com.starry.mb.appointment.dto.AvailabilityVO;
import com.starry.mb.appointment.dto.BookRequest;
import com.starry.mb.appointment.mapper.AppointmentMapper;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.exception.BizException;
import com.starry.mb.employee.domain.Employee;
import com.starry.mb.employee.mapper.EmployeeMapper;
import com.starry.mb.project.domain.Project;
import com.starry.mb.project.mapper.ProjectMapper;
import com.starry.mb.store.domain.Store;
import com.starry.mb.store.mapper.StoreMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
public class AppointmentService {

    /** 时间槽切片粒度（分钟） */
    private static final int SLOT_GRANULARITY_MIN = 30;
    /** 客户自助预约最少需要的提前量（分钟） */
    private static final int MIN_LEAD_MINUTES = 30;
    /** 客户可取消的提前量（分钟） */
    private static final int CANCEL_DEADLINE_MINUTES = 120;

    private static final ObjectMapper JSON = new ObjectMapper();

    private final AppointmentMapper appointmentMapper;
    private final StoreMapper storeMapper;
    private final ProjectMapper projectMapper;
    private final EmployeeMapper employeeMapper;

    public AppointmentService(AppointmentMapper appointmentMapper,
                              StoreMapper storeMapper,
                              ProjectMapper projectMapper,
                              EmployeeMapper employeeMapper) {
        this.appointmentMapper = appointmentMapper;
        this.storeMapper = storeMapper;
        this.projectMapper = projectMapper;
        this.employeeMapper = employeeMapper;
    }

    // ─────────────────────────────────────────────────────────
    //  Availability — 实时计算
    // ─────────────────────────────────────────────────────────

    public AvailabilityVO availability(long storeId, long projectId, LocalDate date) {
        Long tid = requireTenant();
        Store store = mustStore(tid, storeId);
        Project project = mustProject(tid, projectId);

        LocalTime[] hours = businessHoursOf(store, date.getDayOfWeek());
        AvailabilityVO vo = new AvailabilityVO();
        vo.setDate(date);
        vo.setStoreId(storeId);
        vo.setProjectId(projectId);
        vo.setProjectName(project.getName());
        vo.setDurationMin(project.getDurationMin());
        vo.setCapacity(store.getConcurrentCapacity());

        if (hours == null) {
            vo.setSlots(List.of());
            return vo;
        }
        vo.setOpenAt(hours[0]);
        vo.setCloseAt(hours[1]);

        int duration = project.getDurationMin() == null ? 60 : project.getDurationMin();
        int capacity = store.getConcurrentCapacity() == null ? 1 : store.getConcurrentCapacity();

        // 拉当日全部活跃预约，本地算重叠（避免 N+1 query）
        LocalDateTime dayStart = date.atTime(hours[0]);
        LocalDateTime dayEnd   = date.atTime(hours[1]);
        List<Appointment> active = appointmentMapper.listActiveOnDay(tid, storeId, dayStart, dayEnd);

        List<AvailabilityVO.Slot> slots = new ArrayList<>();
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        for (LocalTime t = hours[0]; !t.plusMinutes(duration).isAfter(hours[1]);
                t = t.plusMinutes(SLOT_GRANULARITY_MIN)) {

            LocalDateTime slotStart = date.atTime(t);
            LocalDateTime slotEnd   = slotStart.plusMinutes(duration);

            int used = 0;
            for (Appointment a : active) {
                if (a.getStartAt().isBefore(slotEnd) && a.getEndAt().isAfter(slotStart)) {
                    used++;
                }
            }
            boolean available = used < capacity;

            // 过去时刻 / 当天距离太近，不允许
            if (date.isBefore(today)) {
                available = false;
            } else if (date.equals(today)) {
                if (slotStart.toLocalTime().isBefore(now.plusMinutes(MIN_LEAD_MINUTES))) {
                    available = false;
                }
            }

            slots.add(new AvailabilityVO.Slot(
                    t.toString().substring(0, 5), available, used));
        }
        vo.setSlots(slots);
        return vo;
    }

    // ─────────────────────────────────────────────────────────
    //  下单
    // ─────────────────────────────────────────────────────────

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AppointmentVO book(BookRequest req) {
        Long tid = requireTenant();
        Long cid = requireCustomerId();
        Store store = mustStore(tid, req.getStoreId());
        Project project = mustProject(tid, req.getProjectId());

        LocalDateTime startAt = req.getStartAt();
        if (startAt == null) throw new BizException(40000, "请选择预约时间");
        if (startAt.isBefore(LocalDateTime.now().plusMinutes(MIN_LEAD_MINUTES))) {
            throw new BizException(40000, "至少提前 " + MIN_LEAD_MINUTES + " 分钟预约");
        }

        // slot 对齐：必须是 :00 或 :30
        if (startAt.getMinute() % SLOT_GRANULARITY_MIN != 0 || startAt.getSecond() != 0) {
            throw new BizException(40000, "预约时间必须对齐 30 分钟");
        }

        int duration = project.getDurationMin() == null ? 60 : project.getDurationMin();
        LocalDateTime endAt = startAt.plusMinutes(duration);

        // 落在营业时段内？
        LocalTime[] hours = businessHoursOf(store, startAt.getDayOfWeek());
        if (hours == null
                || startAt.toLocalTime().isBefore(hours[0])
                || endAt.toLocalTime().isAfter(hours[1])
                || !startAt.toLocalDate().equals(endAt.toLocalDate())) {
            throw new BizException(40000, "门店当日不营业或时间超出营业时段");
        }

        // ⚠ 在事务内 FOR UPDATE 锁住重叠区间，防并发超卖
        int capacity = store.getConcurrentCapacity() == null ? 1 : store.getConcurrentCapacity();
        int used = appointmentMapper.countOverlapping(tid, req.getStoreId(), startAt, endAt, "FOR UPDATE");
        if (used >= capacity) {
            throw new BizException(40901, "该时段已被预约满，请换个时间");
        }

        Appointment a = new Appointment();
        a.setStoreId(req.getStoreId());
        a.setCustomerId(cid);
        a.setProjectId(project.getId());
        a.setProjectName(project.getName());
        a.setUnitPrice(project.getUnitPrice());
        a.setStartAt(startAt);
        a.setEndAt(endAt);
        a.setDurationMin(duration);
        a.setPlanItemId(req.getPlanItemId());
        a.setSource(1);
        a.setStatus(AppointmentStatus.PENDING.getCode());
        a.setCustomerNote(req.getCustomerNote());
        appointmentMapper.insert(a);

        return toVO(a);
    }

    // ─────────────────────────────────────────────────────────
    //  查询
    // ─────────────────────────────────────────────────────────

    public List<AppointmentVO> mine() {
        Long tid = requireTenant();
        Long cid = requireCustomerId();
        List<Appointment> list = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getTenantId, tid)
                .eq(Appointment::getCustomerId, cid)
                .orderByDesc(Appointment::getStartAt));
        return list.stream().map(this::toVO).toList();
    }

    public AppointmentVO detail(long id) {
        Long tid = requireTenant();
        Appointment a = appointmentMapper.selectById(id);
        if (a == null || a.getDeletedAt() != null || !a.getTenantId().equals(tid)) {
            throw new BizException(40400, "预约不存在");
        }
        Long cid = PrincipalContext.customerId();
        if (cid != null && !a.getCustomerId().equals(cid)) {
            throw new BizException(40300, "无权访问");
        }
        return toVO(a);
    }

    // ─────────────────────────────────────────────────────────
    //  取消
    // ─────────────────────────────────────────────────────────

    @Transactional
    public void cancelByCustomer(long id, String reason) {
        Long tid = requireTenant();
        Long cid = requireCustomerId();

        Appointment a = appointmentMapper.selectById(id);
        if (a == null || a.getDeletedAt() != null || !a.getTenantId().equals(tid)) {
            throw new BizException(40400, "预约不存在");
        }
        if (!a.getCustomerId().equals(cid)) {
            throw new BizException(40300, "无权操作");
        }
        if (!AppointmentStatus.CUSTOMER_CANCELLABLE.contains(a.getStatus())) {
            throw new BizException(40010, "当前状态无法取消");
        }
        if (a.getStartAt().minusMinutes(CANCEL_DEADLINE_MINUTES).isBefore(LocalDateTime.now())) {
            throw new BizException(40010,
                    "开始前 " + CANCEL_DEADLINE_MINUTES + " 分钟起不可线上取消，请联系顾问");
        }

        a.setStatus(AppointmentStatus.CANCELLED_BY_CUS.getCode());
        a.setCancelledAt(LocalDateTime.now());
        a.setCancelReason(reason);
        appointmentMapper.updateById(a);
    }

    // ─────────────────────────────────────────────────────────
    //  内部工具
    // ─────────────────────────────────────────────────────────

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
        if (a.getConsultantId() != null) {
            Employee e = employeeMapper.selectById(a.getConsultantId());
            if (e != null) vo.setConsultantName(e.getName());
        }

        boolean canCancel = AppointmentStatus.CUSTOMER_CANCELLABLE.contains(a.getStatus())
                && a.getStartAt().minusMinutes(CANCEL_DEADLINE_MINUTES).isAfter(LocalDateTime.now());
        vo.setCanCancel(canCancel);
        return vo;
    }

    private Store mustStore(Long tid, Long storeId) {
        Store s = storeMapper.selectById(storeId);
        if (s == null || s.getDeletedAt() != null || !s.getTenantId().equals(tid)) {
            throw new BizException(40400, "门店不存在");
        }
        return s;
    }

    private Project mustProject(Long tid, Long projectId) {
        Project p = projectMapper.selectById(projectId);
        if (p == null || p.getDeletedAt() != null || !p.getTenantId().equals(tid)) {
            throw new BizException(40400, "项目不存在");
        }
        return p;
    }

    /** 解析 business_hours JSON，返回 [open, close]；当日不营业返回 null。 */
    private LocalTime[] businessHoursOf(Store store, DayOfWeek dow) {
        if (store.getBusinessHours() == null || store.getBusinessHours().isBlank()) {
            return new LocalTime[] { LocalTime.of(10, 0), LocalTime.of(21, 0) };
        }
        try {
            Map<String, List<String>> map = JSON.readValue(store.getBusinessHours(),
                    new TypeReference<>() {});
            String key = switch (dow) {
                case MONDAY -> "mon"; case TUESDAY -> "tue"; case WEDNESDAY -> "wed";
                case THURSDAY -> "thu"; case FRIDAY -> "fri"; case SATURDAY -> "sat";
                case SUNDAY -> "sun";
            };
            List<String> hh = map.get(key);
            if (hh == null || hh.size() < 2) return null;
            return new LocalTime[] { LocalTime.parse(hh.get(0)), LocalTime.parse(hh.get(1)) };
        } catch (Exception e) {
            log.warn("parse business_hours failed, fall back to default. raw={}", store.getBusinessHours());
            return new LocalTime[] { LocalTime.of(10, 0), LocalTime.of(21, 0) };
        }
    }

    private Long requireTenant() {
        Long tid = PrincipalContext.tenantId();
        if (tid == null) throw new BizException(40100, "未登录");
        return tid;
    }

    private Long requireCustomerId() {
        Long cid = PrincipalContext.customerId();
        if (cid == null) throw new BizException(40300, "仅客户可操作");
        return cid;
    }
}
