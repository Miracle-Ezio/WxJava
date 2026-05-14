package com.starry.mb.plan;

import com.starry.mb.common.web.ApiResponse;
import com.starry.mb.plan.dto.PlanDetailVO;
import com.starry.mb.plan.dto.PlanSummaryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    /** 客户：我的全部规划方案。 */
    @GetMapping("/mine")
    public ApiResponse<List<PlanSummaryVO>> mine() {
        return ApiResponse.ok(planService.listMine());
    }

    /** 规划方案详情。 */
    @GetMapping("/{id}")
    public ApiResponse<PlanDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(planService.detail(id));
    }
}
