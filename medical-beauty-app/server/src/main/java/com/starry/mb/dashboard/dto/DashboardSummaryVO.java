package com.starry.mb.dashboard.dto;

import com.starry.mb.appointment.dto.AppointmentVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardSummaryVO {
    /** 今日相关 */
    private Integer todayAppointments;
    private BigDecimal todayRevenue;
    private Integer todayNewCustomers;

    /** 本月 */
    private Integer monthlyNewCustomers;
    private Integer monthlyPlansPushed;
    private BigDecimal monthlyRevenue;

    /** 全局 */
    private Integer totalCustomers;
    private Integer upcomingAppointments;       // 未来 7 天

    /** 14 天日预约趋势 */
    private List<DayPoint> appointmentTrend;
    /** 最近 5 条预约 */
    private List<AppointmentVO> recentAppointments;
    /** 待提醒：未来 24h 内的预约 */
    private List<AppointmentVO> upcomingReminders;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayPoint {
        private String date;     // yyyy-MM-dd
        private Integer count;
    }
}
