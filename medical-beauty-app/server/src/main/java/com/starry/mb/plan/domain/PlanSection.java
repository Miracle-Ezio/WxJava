package com.starry.mb.plan.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.starry.mb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan_section")
public class PlanSection extends BaseEntity {
    private Long planId;
    private Integer sort;
    private String type;
    private String title;
    private String content;
    private String extra;
}
