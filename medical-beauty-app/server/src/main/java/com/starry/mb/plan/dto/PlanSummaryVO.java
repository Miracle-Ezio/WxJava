package com.starry.mb.plan.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PlanSummaryVO {
    private Long id;
    private String title;
    private String subtitle;
    private String coverUrl;
    private Integer status;
    private LocalDate validFrom;
    private LocalDate validTo;
    private BigDecimal totalPrice;
    private BigDecimal discountPrice;
    private LocalDateTime pushedAt;
    private Integer totalItems;
    private Integer doneItems;
}
