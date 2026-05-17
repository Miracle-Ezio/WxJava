package com.starry.mb.store.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.starry.mb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store")
public class Store extends BaseEntity {
    /** store 本身就是门店，没有 store_id 列；V1 也没建 created_by */
    @TableField(exist = false)
    private Long storeId;
    @TableField(exist = false)
    private Long createdBy;

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
