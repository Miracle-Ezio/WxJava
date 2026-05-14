package com.starry.mb.photo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PhotoVO {
    private Long id;
    private Long customerId;
    private Integer pose;
    private String poseLabel;
    private String bodyPart;
    private LocalDateTime shotAt;
    /** 实时签名 URL（含 ttl） */
    private String url;
    private String thumbUrl;
    private Integer width;
    private Integer height;
    private Integer visibility;
    private Integer locked;
    private String remark;
}
