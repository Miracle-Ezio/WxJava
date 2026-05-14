package com.starry.mb.photo.domain;

import lombok.Getter;

@Getter
public enum Pose {
    FRONT(1,       "正面"),
    LEFT_45(2,    "左 45°"),
    RIGHT_45(3,   "右 45°"),
    TOP_LIGHT(4,  "顶光"),
    FACE_CLOSE(5, "全脸特写"),
    PART(6,       "局部");

    private final int code;
    private final String label;

    Pose(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static Pose ofCode(int code) {
        for (Pose p : values()) if (p.code == code) return p;
        throw new IllegalArgumentException("unknown pose code: " + code);
    }
}
