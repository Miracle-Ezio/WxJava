package com.starry.mb.customer.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerSummaryVO {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String realName;
    private Integer gender;
    private String levelCode;
    private BigDecimal totalRecharge;
    private BigDecimal balance;
    private LocalDateTime lastVisitAt;
    private LocalDateTime firstVisitAt;
    private String consultantName;
}
