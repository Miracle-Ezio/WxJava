package com.starry.mb.plan.dto;

import com.starry.mb.plan.domain.Plan;
import com.starry.mb.plan.domain.PlanItem;
import com.starry.mb.plan.domain.PlanSection;
import lombok.Data;

import java.util.List;

@Data
public class PlanDetailVO {
    private Plan plan;
    private List<PlanSection> sections;
    private List<PlanItem> items;
    private String consultantName;
}
