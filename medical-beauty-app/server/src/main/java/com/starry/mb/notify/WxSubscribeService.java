package com.starry.mb.notify;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starry.mb.customer.domain.Customer;
import com.starry.mb.customer.mapper.CustomerMapper;
import com.starry.mb.notify.domain.SubscribeMsgLog;
import com.starry.mb.notify.mapper.SubscribeMsgLogMapper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 订阅消息发送服务。
 *
 * 设计要点：
 *   - 所有发送都先入 subscribe_msg_log（成功/失败都记），便于排查
 *   - 模板 ID 用占位（TPL_*）时不真实发送，仅打日志，让演示环境也能跑
 *   - 调用方场景：
 *       1) AdminAppointment 状态机：确认/到店/完成时主动发
 *       2) AdminPlanService：推送规划方案时发
 *       3) AppointmentReminderScheduler：到店前 24h/2h 自动扫
 */
@Slf4j
@Service
public class WxSubscribeService {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final WxMaService wxMaService;
    private final CustomerMapper customerMapper;
    private final SubscribeMsgLogMapper logMapper;
    private final SubscribeMsgProperties props;

    public WxSubscribeService(WxMaService wxMaService,
                              CustomerMapper customerMapper,
                              SubscribeMsgLogMapper logMapper,
                              SubscribeMsgProperties props) {
        this.wxMaService = wxMaService;
        this.customerMapper = customerMapper;
        this.logMapper = logMapper;
        this.props = props;
    }

    public void notifyAppointmentConfirmed(Long appointmentId, Long customerId,
                                           String project, String when, String store) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("thing1", clip(project, 20));
        data.put("date2", when);
        data.put("thing3", clip(store, 20));
        data.put("thing4", "门店已确认，请按时到店");
        send(customerId, props.getAppointmentConfirmed(),
                props.getAppointmentPage() + "?id=" + appointmentId,
                "appointment_confirmed", "appointment", appointmentId, data);
    }

    public void notifyAppointmentReminder(Long appointmentId, Long customerId,
                                          String project, String when, String store,
                                          boolean is2h) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("thing1", clip(project, 20));
        data.put("date2", when);
        data.put("thing3", clip(store, 20));
        data.put("thing4", is2h ? "您的预约 2 小时后开始" : "明日预约提醒");
        String tplId = is2h ? props.getReminder2h() : props.getReminder24h();
        String scene = is2h ? "reminder_2h" : "reminder_24h";
        send(customerId, tplId,
                props.getAppointmentPage() + "?id=" + appointmentId,
                scene, "appointment", appointmentId, data);
    }

    public void notifyPlanPushed(Long planId, Long customerId, String title) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("thing1", clip(title, 20));
        data.put("thing2", "您的专属规划方案已就绪");
        data.put("date3", LocalDateTime.now().toString().substring(0, 16).replace('T', ' '));
        send(customerId, props.getPlanPushed(),
                props.getPlanPage() + "?id=" + planId,
                "plan_pushed", "plan", planId, data);
    }

    // ─────────────────────────────────────────────────────────

    private void send(Long customerId, String templateId, String page, String scene,
                      String refType, Long refId, Map<String, String> data) {
        Customer c = customerMapper.selectById(customerId);
        if (c == null || c.getOpenid() == null || c.getOpenid().isBlank()) {
            log.info("[WxSubscribe] skip {}: customer/openid missing", scene);
            return;
        }

        SubscribeMsgLog rec = new SubscribeMsgLog();
        rec.setTenantId(c.getTenantId());
        rec.setCustomerId(customerId);
        rec.setOpenid(c.getOpenid());
        rec.setTemplateId(templateId);
        rec.setScene(scene);
        rec.setRefType(refType);
        rec.setRefId(refId);
        try { rec.setDataJson(JSON.writeValueAsString(data)); } catch (Exception ignored) {}

        // 占位模板（演示场景）：只记日志、不真实调用微信
        if (templateId == null || templateId.startsWith("TPL_")) {
            rec.setSuccess(1);
            rec.setWxErrmsg("DEMO: not sent, template id is placeholder");
            logMapper.insert(rec);
            log.info("[WxSubscribe] (demo) {} → customer #{} | tpl={} | data={}",
                    scene, customerId, templateId, data);
            return;
        }

        // 真实发送
        WxMaSubscribeMessage msg = WxMaSubscribeMessage.builder()
                .toUser(c.getOpenid())
                .templateId(templateId)
                .page(page)
                .miniprogramState("formal")
                .lang("zh_CN")
                .build();
        data.forEach((k, v) -> msg.addData(new WxMaSubscribeMessage.MsgData(k, v)));

        try {
            wxMaService.getMsgService().sendSubscribeMsg(msg);
            rec.setSuccess(1);
        } catch (WxErrorException e) {
            rec.setSuccess(0);
            rec.setWxErrcode(e.getError() == null ? null : e.getError().getErrorCode());
            rec.setWxErrmsg(e.getError() == null ? e.getMessage() : e.getError().getErrorMsg());
            log.warn("[WxSubscribe] send {} failed: {}", scene, rec.getWxErrmsg());
        } catch (Exception e) {
            rec.setSuccess(0);
            rec.setWxErrmsg(e.getMessage());
            log.warn("[WxSubscribe] send {} unexpected error", scene, e);
        }
        logMapper.insert(rec);
    }

    private String clip(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
