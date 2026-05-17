package com.starry.mb.plan.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.starry.mb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan_item")
public class PlanItem extends BaseEntity {
    /** plan_item 表本身没 store_id 列 */
    @TableField(exist = false)
    private Long storeId;

    private Long planId;
    private Long sectionId;
    private Long projectId;
    private String projectName;
    private Integer plannedCount;
    private Integer doneCount;
    private String cycleRule;
    private BigDecimal unitPrice;
    private BigDecimal activityPrice;
    private BigDecimal totalPrice;
    private LocalDateTime nextDueAt;
    private Integer status;
    private String notes;
}
