package com.starry.mb.photo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.starry.mb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_photo")
public class CustomerPhoto extends BaseEntity {
    private Long customerId;
    /** {@link Pose#getCode()} */
    private Integer pose;
    private String bodyPart;
    private LocalDateTime shotAt;
    private String objectKey;
    private String thumbKey;
    private Integer width;
    private Integer height;
    private Integer sizeBytes;
    private Long treatmentRecordId;
    /** 1 客户 / 2 员工 */
    private Integer uploadedBy;
    /** 1 仅自己 / 2 自己+顾问 / 3 全店 */
    private Integer visibility;
    /** 客户单独锁定，员工不可见 */
    private Integer locked;
    private String aiFaceLandmarks;
    private String remark;
}
