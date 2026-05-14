package com.starry.mb.photo.dto;

import lombok.Data;

@Data
public class CompareVO {
    private PhotoVO before;
    private PhotoVO after;
    /** 同机位首张 / 最新一张的快捷模式 */
    private String mode;
}
