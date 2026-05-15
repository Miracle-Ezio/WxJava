package com.starry.mb.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starry.mb.appointment.domain.Appointment;
import com.starry.mb.appointment.domain.AppointmentStatus;
import com.starry.mb.appointment.dto.AppointmentVO;
import com.starry.mb.appointment.mapper.AppointmentMapper;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.web.ApiResponse;
import com.starry.mb.customer.domain.Customer;
import com.starry.mb.customer.mapper.CustomerMapper;
import com.starry.mb.dashboard.dto.DashboardSummaryVO;
import com.starry.mb.dashboard.dto.DashboardSummaryVO.DayPoint;
import com.starry.mb.plan.domain.Plan;
import com.starry.mb.plan.mapper.PlanMapper;
import com.starry.mb.store.mapper.StoreMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AppointmentMapper appointmentMapper;
    private final CustomerMapper customerMapper;
    private final PlanMapper planMapper;
    private final StoreMapper storeMapper;

    public AdminDashboardController(AppointmentMapper appointmentMapper,
                                    CustomerMapper customerMapper,
                                    PlanMapper planMapper,
                                    StoreMapper storeMapper) {
        this.appointmentMapper = appointmentMapper;
        this.customerMapper = customerMapper;
        this.planMapper = planMapper;
        this.storeMapper = storeMapper;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryVO> summary() {
        Long tid = PrincipalContext.tenantId();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime now = LocalDateTime.now();

        // 拉本月以来的预约一次，本地聚合（数据量不大的早期阶段，比 SQL group by 灵活）
        List<Appointment> monthAppts = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getTenantId, tid)
                .ge(Appointment::getStartAt, monthStart.atStartOfDay()));

        // 14 天趋势：单独再取近 14 天
        LocalDate trendStart = today.minusDays(13);
        List<Appointment> trendAppts = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getTenantId, tid)
                .ge(Appointment::getStartAt, trendStart.atStartOfDay())
                .lt(Appointment::getStartAt, today.plusDays(1).atStartOfDay()));

        Map<String, Long> trendByDay = trendAppts.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getStartAt().toLocalDate().format(D),
                        Collectors.counting()));
        List<DayPoint> trend = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            String d = trendStart.plusDays(i).format(D);
            trend.add(new DayPoint(d, trendByDay.getOrDefault(d, 0L).intValue()));
        }

        // 今日预约 / 营业额 / 月度
        int todayApptCount = 0;
        BigDecimal todayRevenue = BigDecimal.ZERO;
        BigDecimal monthlyRevenue = BigDecimal.ZERO;
        for (Appointment a : monthAppts) {
            LocalDate d = a.getStartAt().toLocalDate();
            if (d.equals(today) && !isCancelled(a.getStatus())) todayApptCount++;
            if (a.getStatus() != null && a.getStatus() == AppointmentStatus.COMPLETED.getCode()
                    && a.getUnitPrice() != null) {
                if (d.equals(today))    todayRevenue   = todayRevenue.add(a.getUnitPrice());
                monthlyRevenue = monthlyRevenue.add(a.getUnitPrice());
            }
        }

        // 客户统计
        Integer totalCustomers = Math.toIntExact(customerMapper.selectCount(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getTenantId, tid).eq(Customer::getStatus, 1)));
        Integer monthlyNewCustomers = Math.toIntExact(customerMapper.selectCount(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getTenantId, tid)
                .ge(Customer::getFirstVisitAt, monthStart.atStartOfDay())));
        Integer todayNewCustomers = Math.toIntExact(customerMapper.selectCount(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getTenantId, tid)
                .ge(Customer::getFirstVisitAt, today.atStartOfDay())));

        // 本月推送的规划方案
        Integer monthlyPlansPushed = Math.toIntExact(planMapper.selectCount(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getTenantId, tid)
                .ge(Plan::getPushedAt, monthStart.atStartOfDay())));

        // 未来 7 天预约
        Integer upcoming = Math.toIntExact(appointmentMapper.selectCount(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getTenantId, tid)
                .ge(Appointment::getStartAt, now)
                .lt(Appointment::getStartAt, now.plusDays(7))
                .in(Appointment::getStatus, AppointmentStatus.ACTIVE)));

        // 最近 5 条预约 + 未来 24h 待提醒
        List<Appointment> recent = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getTenantId, tid)
                .orderByDesc(Appointment::getCreatedAt)
                .last("LIMIT 5"));
        List<Appointment> reminders = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getTenantId, tid)
                .ge(Appointment::getStartAt, now)
                .lt(Appointment::getStartAt, now.plusHours(24))
                .in(Appointment::getStatus, AppointmentStatus.ACTIVE)
                .orderByAsc(Appointment::getStartAt));

        DashboardSummaryVO vo = new DashboardSummaryVO();
        vo.setTodayAppointments(todayApptCount);
        vo.setTodayRevenue(todayRevenue);
        vo.setTodayNewCustomers(todayNewCustomers);
        vo.setMonthlyNewCustomers(monthlyNewCustomers);
        vo.setMonthlyPlansPushed(monthlyPlansPushed);
        vo.setMonthlyRevenue(monthlyRevenue);
        vo.setTotalCustomers(totalCustomers);
        vo.setUpcomingAppointments(upcoming);
        vo.setAppointmentTrend(trend);
        vo.setRecentAppointments(recent.stream().map(this::toAppointmentVO).toList());
        vo.setUpcomingReminders(reminders.stream().map(this::toAppointmentVO).toList());
        return ApiResponse.ok(vo);
    }

    private boolean isCancelled(Integer status) {
        return status != null && (
                status == AppointmentStatus.CANCELLED_BY_CUS.getCode() ||
                status == AppointmentStatus.CANCELLED_BY_BIZ.getCode() ||
                status == AppointmentStatus.NO_SHOW.getCode());
    }

    private AppointmentVO toAppointmentVO(Appointment a) {
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
        if (a.getStoreId() != null) {
            var s = storeMapper.selectById(a.getStoreId());
            if (s != null) vo.setStoreName(s.getName());
        }
        if (a.getCustomerId() != null) {
            Customer c = customerMapper.selectById(a.getCustomerId());
            if (c != null) vo.setCustomerName(c.getRealName() != null ? c.getRealName() : c.getNickname());
        }
        return vo;
    }
}
