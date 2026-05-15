package com.starry.mb.appointment;

import com.starry.mb.appointment.dto.AppointmentVO;
import com.starry.mb.common.web.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/appointments")
public class AdminAppointmentController {

    private final AdminAppointmentService service;

    public AdminAppointmentController(AdminAppointmentService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<AdminAppointmentService.ListResult> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1")  Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return ApiResponse.ok(service.list(status, from, to, keyword, page, size));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<AppointmentVO> confirm(@PathVariable long id,
                                              @RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.ok(service.confirm(id, body == null ? null : body.get("staffNote")));
    }

    @PostMapping("/{id}/check-in")
    public ApiResponse<AppointmentVO> checkIn(@PathVariable long id) {
        return ApiResponse.ok(service.checkIn(id));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<AppointmentVO> complete(@PathVariable long id,
                                               @RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.ok(service.complete(id, body == null ? null : body.get("staffNote")));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<AppointmentVO> cancel(@PathVariable long id,
                                             @RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.ok(service.cancelByBiz(id, body == null ? null : body.get("reason")));
    }

    @PostMapping("/{id}/no-show")
    public ApiResponse<AppointmentVO> noShow(@PathVariable long id) {
        return ApiResponse.ok(service.markNoShow(id));
    }

    @PutMapping("/{id}/staff-note")
    public ApiResponse<AppointmentVO> staffNote(@PathVariable long id,
                                                @RequestBody Map<String, String> body) {
        return ApiResponse.ok(service.updateStaffNote(id, body.get("staffNote")));
    }
}
