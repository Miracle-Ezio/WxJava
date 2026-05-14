package com.starry.mb.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class AvailabilityVO {
    private LocalDate date;
    private Long storeId;
    private Long projectId;
    private String projectName;
    private Integer durationMin;
    private List<Slot> slots;
    /** 营业时段（用于前端展示） */
    private LocalTime openAt;
    private LocalTime closeAt;
    /** 该门店并发上限 */
    private Integer capacity;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Slot {
        /** 该 slot 起始时刻（"HH:mm"） */
        private String time;
        private Boolean available;
        /** 当前并发数（在该 slot 起始时） */
        private Integer used;
    }
}
