package com.starry.mb.appointment;

import com.starry.mb.appointment.dto.AppointmentVO;
import com.starry.mb.appointment.dto.AvailabilityVO;
import com.starry.mb.appointment.dto.BookRequest;
import com.starry.mb.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    /** 指定门店 / 项目 / 日期 的可约时段。 */
    @GetMapping("/availability")
    public ApiResponse<AvailabilityVO> availability(
            @RequestParam long storeId,
            @RequestParam long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(service.availability(storeId, projectId, date));
    }

    /** 客户提交自助预约。 */
    @PostMapping
    public ApiResponse<AppointmentVO> book(@Valid @RequestBody BookRequest req) {
        return ApiResponse.ok(service.book(req));
    }

    @GetMapping("/mine")
    public ApiResponse<List<AppointmentVO>> mine() {
        return ApiResponse.ok(service.mine());
    }

    @GetMapping("/{id}")
    public ApiResponse<AppointmentVO> detail(@PathVariable long id) {
        return ApiResponse.ok(service.detail(id));
    }

    /** 客户取消。 */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable long id,
                                    @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        service.cancelByCustomer(id, reason);
        return ApiResponse.ok();
    }
}
