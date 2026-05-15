package com.starry.mb.appointment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starry.mb.appointment.domain.Appointment;
import com.starry.mb.appointment.domain.AppointmentStatus;
import com.starry.mb.appointment.mapper.AppointmentMapper;
import com.starry.mb.notify.WxSubscribeService;
import com.starry.mb.store.domain.Store;
import com.starry.mb.store.mapper.StoreMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 扫描即将到店的预约，发订阅消息提醒。
 *
 *   - 24h 前一次提醒：[now+24h-30min, now+24h+30min] 窗口
 *   - 2h 前一次提醒： [now+2h-15min,  now+2h+15min]  窗口
 *
 * 每条预约只发一次，靠 reminded_24h_at / reminded_2h_at 标记防重。
 */
@Slf4j
@Component
public class AppointmentReminderScheduler {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("MM 月 dd 日 HH:mm");

    private final AppointmentMapper appointmentMapper;
    private final StoreMapper storeMapper;
    private final WxSubscribeService wxSubscribe;

    public AppointmentReminderScheduler(AppointmentMapper appointmentMapper,
                                        StoreMapper storeMapper,
                                        WxSubscribeService wxSubscribe) {
        this.appointmentMapper = appointmentMapper;
        this.storeMapper = storeMapper;
        this.wxSubscribe = wxSubscribe;
    }

    /** 每 10 分钟扫一次 24h 提醒。 */
    @Scheduled(cron = "0 */10 * * * *")
    public void scan24h() {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> due = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .in(Appointment::getStatus, AppointmentStatus.ACTIVE)
                .ge(Appointment::getStartAt, now.plusHours(24).minusMinutes(30))
                .lt(Appointment::getStartAt, now.plusHours(24).plusMinutes(30))
                .isNull(Appointment::getReminded24hAt));
        send(due, false);
    }

    /** 每 5 分钟扫一次 2h 提醒。 */
    @Scheduled(cron = "0 */5 * * * *")
    public void scan2h() {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> due = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .in(Appointment::getStatus, AppointmentStatus.ACTIVE)
                .ge(Appointment::getStartAt, now.plusHours(2).minusMinutes(15))
                .lt(Appointment::getStartAt, now.plusHours(2).plusMinutes(15))
                .isNull(Appointment::getReminded2hAt));
        send(due, true);
    }

    private void send(List<Appointment> due, boolean is2h) {
        if (due.isEmpty()) return;
        log.info("[Reminder] {} appointments due for {}h reminder", due.size(), is2h ? "2" : "24");
        for (Appointment a : due) {
            try {
                Store store = storeMapper.selectById(a.getStoreId());
                wxSubscribe.notifyAppointmentReminder(
                        a.getId(), a.getCustomerId(),
                        a.getProjectName(),
                        a.getStartAt().format(WHEN),
                        store == null ? "" : store.getName(),
                        is2h);

                if (is2h) a.setReminded2hAt(LocalDateTime.now());
                else      a.setReminded24hAt(LocalDateTime.now());
                appointmentMapper.updateById(a);
            } catch (Exception e) {
                log.warn("[Reminder] appointment #{} failed", a.getId(), e);
            }
        }
    }
}
