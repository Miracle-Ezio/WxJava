package com.starry.mb.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 顾问端创建 / 更新规划方案的载荷。
 * 一次性提交：plan 主信息 + 全部章节 + 全部项目，后端整体替换。
 */
@Data
public class PlanUpsertRequest {
    @NotNull
    private Long customerId;

    @NotBlank
    private String title;
    private String subtitle;
    private String coverUrl;
    private LocalDate validFrom;
    private LocalDate validTo;
    private BigDecimal totalPrice;
    private BigDecimal discountPrice;
    private String analysisText;

    private List<SectionDTO> sections;
    private List<ItemDTO> items;

    @Data
    public static class SectionDTO {
        private Long id;            // 编辑时回填
        private Integer sort;
        private String type;        // analysis/region_plan/project_list/material/package/case
        private String title;
        private String content;
    }

    @Data
    public static class ItemDTO {
        private Long id;
        private Long sectionId;
        private Long projectId;
        private String projectName;
        private Integer plannedCount;
        private BigDecimal unitPrice;
        private BigDecimal activityPrice;
        private BigDecimal totalPrice;
        private String notes;
    }
}
