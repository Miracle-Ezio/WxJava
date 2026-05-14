package com.starry.mb.employee.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.starry.mb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("employee")
public class Employee extends BaseEntity {
    private String name;
    private String phone;
    private String phoneHash;
    private String passwordHash;
    private String avatarUrl;
    private String jobTitle;
    private String roleCode;
    private BigDecimal commissionRate;
    private String wxOpenid;
    private Integer status;
}
