package com.starry.mb.notify.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("subscribe_msg_log")
public class SubscribeMsgLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;
    private Long customerId;
    private String openid;
    private String templateId;
    private String scene;
    private String refType;
    private Long refId;
    private String dataJson;
    private Integer success;
    private Integer wxErrcode;
    private String wxErrmsg;
    private LocalDateTime createdAt;
}
