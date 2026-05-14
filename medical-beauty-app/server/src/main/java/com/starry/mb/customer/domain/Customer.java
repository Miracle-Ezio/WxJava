package com.starry.mb.customer.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.starry.mb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class Customer extends BaseEntity {
    private String unionid;
    private String openid;
    private String nickname;
    private String avatarUrl;
    private String realName;
    private Integer gender;
    private LocalDate birthday;
    private String phone;
    private String phoneHash;
    private String idCard;
    private Integer skinType;
    private String allergy;
    private String medicalHistory;
    private String emergencyContact;
    private String levelCode;
    private BigDecimal totalRecharge;
    private BigDecimal balance;
    private Long consultantId;
    private String source;
    private LocalDateTime firstVisitAt;
    private LocalDateTime lastVisitAt;
    private Integer status;
    private String remark;
}
