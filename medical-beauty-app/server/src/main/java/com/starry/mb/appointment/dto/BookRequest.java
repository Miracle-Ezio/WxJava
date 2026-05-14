package com.starry.mb.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookRequest {
    @NotNull
    private Long projectId;
    @NotNull
    private Long storeId;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startAt;

    /** 可选：关联规划方案项目，完成时推进 done_count */
    private Long planItemId;

    private String customerNote;
}
