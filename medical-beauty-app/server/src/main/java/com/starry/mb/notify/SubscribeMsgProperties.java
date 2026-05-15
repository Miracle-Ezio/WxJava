package com.starry.mb.notify;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 订阅消息模板 ID 配置。
 *
 * 真实模板需在微信公众平台「小程序 / 订阅消息」中申请，按场景配置后填入。
 * 未配置（占位 starts with "TPL_")时 WxSubscribeService 仅记录日志、不发送。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "starry.subscribe-msg")
public class SubscribeMsgProperties {
    /** 预约已确认 */
    private String appointmentConfirmed = "TPL_APPT_CONFIRMED";
    /** 到店前 24h 提醒 */
    private String reminder24h = "TPL_REMINDER_24H";
    /** 到店前 2h 提醒 */
    private String reminder2h = "TPL_REMINDER_2H";
    /** 规划方案已推送 */
    private String planPushed = "TPL_PLAN_PUSHED";

    /** 跳转的小程序页面 */
    private String appointmentPage = "pages/appointment/detail";
    private String planPage = "pages/plan/detail";
}
