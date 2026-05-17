package com.starry.mb.plan.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.starry.mb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan_section")
public class PlanSection extends BaseEntity {
    /** plan_section 表本身没 store_id 列 */
    @TableField(exist = false)
    private Long storeId;

    private Long planId;
    private Integer sort;
    private String type;
    private String title;
    private String content;
    private String extra;
}
