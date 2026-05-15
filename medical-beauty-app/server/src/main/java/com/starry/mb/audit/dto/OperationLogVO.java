package com.starry.mb.audit.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogVO {
    private Long id;
    private Integer operatorType;
    private String operatorTypeLabel;
    private Long operatorId;
    private String operatorName;
    private String module;
    private String moduleLabel;
    private String action;
    private String actionLabel;
    private Long targetId;
    private String targetSummary;
    private String ip;
    private LocalDateTime createdAt;
}
