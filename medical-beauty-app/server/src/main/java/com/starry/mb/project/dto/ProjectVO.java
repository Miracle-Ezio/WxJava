package com.starry.mb.project.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjectVO {
    private Long id;
    private String name;
    private String category;
    private String coverUrl;
    private String description;
    private Integer durationMin;
    private BigDecimal unitPrice;
}
