package com.starry.mb.photo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * multipart 上传时的表单字段（file 在 @RequestPart 中单独接）。
 */
@Data
public class UploadPhotoRequest {
    @NotNull @Min(1)
    private Integer pose;
    private String bodyPart;
    private Long treatmentRecordId;
    /** 1 仅自己 / 2 自己+顾问 / 3 全店；默认 2 */
    private Integer visibility = 2;
    private String remark;
}
