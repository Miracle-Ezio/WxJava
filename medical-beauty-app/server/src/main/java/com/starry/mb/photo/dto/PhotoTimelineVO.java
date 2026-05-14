package com.starry.mb.photo.dto;

import lombok.Data;

import java.util.List;

@Data
public class PhotoTimelineVO {
    /** 形如 "2026-05" */
    private String month;
    private List<PhotoVO> photos;
}
