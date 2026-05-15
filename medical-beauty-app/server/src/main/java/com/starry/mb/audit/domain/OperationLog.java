package com.starry.mb.audit.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;
    private Long storeId;

    /** 1 员工 / 2 客户 / 3 系统 */
    private Integer operatorType;
    private Long operatorId;
    private String operatorName;

    private String module;
    private String action;
    private Long targetId;
    private String targetSummary;

    private String beforeJson;
    private String afterJson;

    private String ip;
    private String userAgent;

    private LocalDateTime createdAt;
}
