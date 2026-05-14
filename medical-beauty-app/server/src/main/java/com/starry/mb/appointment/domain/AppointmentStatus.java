package com.starry.mb.appointment.domain;

import lombok.Getter;

import java.util.Set;

@Getter
public enum AppointmentStatus {
    PENDING(1,        "待确认"),
    CONFIRMED(2,      "已确认"),
    CHECKED_IN(3,     "已到店"),
    COMPLETED(4,      "已完成"),
    CANCELLED_BY_CUS(5, "客户取消"),
    CANCELLED_BY_BIZ(6, "机构取消"),
    NO_SHOW(7,        "未到");

    private final int code;
    private final String label;

    AppointmentStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AppointmentStatus ofCode(int code) {
        for (AppointmentStatus s : values()) if (s.code == code) return s;
        throw new IllegalArgumentException("unknown appointment status: " + code);
    }

    /** 占用 schedule 槽位的状态（用于并发判定 / availability 计算） */
    public static final Set<Integer> ACTIVE = Set.of(
            PENDING.code, CONFIRMED.code, CHECKED_IN.code);

    /** 客户可取消的状态 */
    public static final Set<Integer> CUSTOMER_CANCELLABLE = Set.of(
            PENDING.code, CONFIRMED.code);
}
