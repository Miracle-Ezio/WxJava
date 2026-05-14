package com.starry.mb.plan.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.starry.mb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan")
public class Plan extends BaseEntity {
    private Long customerId;
    private String title;
    private String subtitle;
    private String coverUrl;
    private Long consultantId;
    private LocalDate validFrom;
    private LocalDate validTo;
    private BigDecimal totalPrice;
    private BigDecimal discountPrice;
    private String analysisText;
    private String analysisPhotoKeys;
    private Integer status;
    private Integer version;
    private LocalDateTime pushedAt;
}
