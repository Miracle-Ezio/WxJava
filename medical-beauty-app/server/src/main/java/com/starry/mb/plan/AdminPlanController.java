package com.starry.mb.plan;

import com.starry.mb.common.web.ApiResponse;
import com.starry.mb.plan.domain.Plan;
import com.starry.mb.plan.dto.PlanDetailVO;
import com.starry.mb.plan.dto.PlanUpsertRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/plans")
public class AdminPlanController {

    private final AdminPlanService service;

    public AdminPlanController(AdminPlanService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Plan>> listByCustomer(@RequestParam long customerId) {
        return ApiResponse.ok(service.listByCustomer(customerId));
    }

    @GetMapping("/{id}")
    public ApiResponse<PlanDetailVO> detail(@PathVariable long id) {
        return ApiResponse.ok(service.detail(id));
    }

    @PostMapping
    public ApiResponse<Plan> create(@Valid @RequestBody PlanUpsertRequest req) {
        return ApiResponse.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<Plan> update(@PathVariable long id,
                                    @Valid @RequestBody PlanUpsertRequest req) {
        return ApiResponse.ok(service.update(id, req));
    }

    /** 推送给客户：草稿 → 已推送，写入 pushed_at。 */
    @PostMapping("/{id}/push")
    public ApiResponse<Plan> push(@PathVariable long id) {
        return ApiResponse.ok(service.push(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id) {
        service.softDelete(id);
        return ApiResponse.ok();
    }
}
