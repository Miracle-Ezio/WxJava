package com.starry.mb.project.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.starry.mb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project")
public class Project extends BaseEntity {
    private String name;
    private String category;
    private String coverUrl;
    private String description;
    private String scienceText;
    private Integer durationMin;
    /** JSON: {"phase1": {"count": 3, "interval_days": 30}, ...} */
    private String defaultCycleRule;
    private BigDecimal unitPrice;
    private BigDecimal commissionRate;
    private Integer status;
}
