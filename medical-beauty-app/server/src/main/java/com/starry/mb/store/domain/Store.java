package com.starry.mb.store.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.starry.mb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store")
public class Store extends BaseEntity {
    private String name;
    private String address;
    private String phone;
    private BigDecimal lat;
    private BigDecimal lng;
    /** JSON: {"mon": ["10:00","21:00"], ...} */
    private String businessHours;
    /** 同时段并发接待上限 */
    private Integer concurrentCapacity;
    private Integer status;
}
