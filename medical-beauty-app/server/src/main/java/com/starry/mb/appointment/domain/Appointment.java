package com.starry.mb.appointment.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.starry.mb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("appointment")
public class Appointment extends BaseEntity {
    private Long customerId;
    private Long projectId;
    private String projectName;
    private BigDecimal unitPrice;

    private Long consultantId;
    private Long operatorId;
    private Long planItemId;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer durationMin;

    /** 1 客户自助 / 2 顾问代约 / 3 到店现约 */
    private Integer source;
    /** {@link AppointmentStatus#getCode()} */
    private Integer status;

    private String customerNote;
    private String staffNote;
    private String cancelReason;

    private LocalDateTime confirmedAt;
    private LocalDateTime checkedInAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime reminded24hAt;
    private LocalDateTime reminded2hAt;
}
