package com.starry.mb.appointment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AppointmentVO {
    private Long id;
    private Long storeId;
    private String storeName;
    private Long projectId;
    private String projectName;
    private BigDecimal unitPrice;
    private Integer durationMin;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer status;
    private String statusLabel;
    private Integer source;
    private String customerNote;
    private String cancelReason;
    private String consultantName;
    /** 客户姓名快照（admin 端展示用） */
    private String customerName;
    /** 客户当前是否可自行取消 */
    private Boolean canCancel;
}
