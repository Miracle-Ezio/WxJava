package com.starry.mb.plan;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.exception.BizException;
import com.starry.mb.employee.domain.Employee;
import com.starry.mb.employee.mapper.EmployeeMapper;
import com.starry.mb.plan.domain.Plan;
import com.starry.mb.plan.domain.PlanItem;
import com.starry.mb.plan.domain.PlanSection;
import com.starry.mb.plan.dto.PlanDetailVO;
import com.starry.mb.plan.dto.PlanSummaryVO;
import com.starry.mb.plan.mapper.PlanItemMapper;
import com.starry.mb.plan.mapper.PlanMapper;
import com.starry.mb.plan.mapper.PlanSectionMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlanService {

    private final PlanMapper planMapper;
    private final PlanSectionMapper sectionMapper;
    private final PlanItemMapper itemMapper;
    private final EmployeeMapper employeeMapper;

    public PlanService(PlanMapper planMapper,
                       PlanSectionMapper sectionMapper,
                       PlanItemMapper itemMapper,
                       EmployeeMapper employeeMapper) {
        this.planMapper = planMapper;
        this.sectionMapper = sectionMapper;
        this.itemMapper = itemMapper;
        this.employeeMapper = employeeMapper;
    }

    /** 客户视角：拉自己的全部规划方案（已推送以上状态）。 */
    public List<PlanSummaryVO> listMine() {
        Long customerId = PrincipalContext.customerId();
        Long tenantId = PrincipalContext.tenantId();
        if (customerId == null) throw new BizException(40100, "未登录");

        List<Plan> plans = planMapper.selectList(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getTenantId, tenantId)
                .eq(Plan::getCustomerId, customerId)
                .ge(Plan::getStatus, 2)
                .orderByDesc(Plan::getPushedAt));

        List<PlanSummaryVO> list = new ArrayList<>(plans.size());
        for (Plan p : plans) {
            PlanSummaryVO vo = new PlanSummaryVO();
            vo.setId(p.getId());
            vo.setTitle(p.getTitle());
            vo.setSubtitle(p.getSubtitle());
            vo.setCoverUrl(p.getCoverUrl());
            vo.setStatus(p.getStatus());
            vo.setValidFrom(p.getValidFrom());
            vo.setValidTo(p.getValidTo());
            vo.setTotalPrice(p.getTotalPrice());
            vo.setDiscountPrice(p.getDiscountPrice());
            vo.setPushedAt(p.getPushedAt());

            List<PlanItem> items = itemMapper.selectList(new LambdaQueryWrapper<PlanItem>()
                    .eq(PlanItem::getPlanId, p.getId()));
            vo.setTotalItems(items.stream().mapToInt(PlanItem::getPlannedCount).sum());
            vo.setDoneItems(items.stream().mapToInt(PlanItem::getDoneCount).sum());
            list.add(vo);
        }
        return list;
    }

    /** 详情：客户只能看自己的；员工可看本租户内的。 */
    public PlanDetailVO detail(Long planId) {
        Long tenantId = PrincipalContext.tenantId();
        Plan plan = planMapper.selectById(planId);
        if (plan == null || plan.getDeletedAt() != null || !plan.getTenantId().equals(tenantId)) {
            throw new BizException(40400, "规划方案不存在");
        }

        Long customerId = PrincipalContext.customerId();
        if (customerId != null && !plan.getCustomerId().equals(customerId)) {
            throw new BizException(40300, "无权访问");
        }
        // 草稿状态对客户不可见
        if (customerId != null && plan.getStatus() < 2) {
            throw new BizException(40300, "方案尚未发布");
        }

        List<PlanSection> sections = sectionMapper.selectList(new LambdaQueryWrapper<PlanSection>()
                .eq(PlanSection::getPlanId, planId)
                .orderByAsc(PlanSection::getSort));

        List<PlanItem> items = itemMapper.selectList(new LambdaQueryWrapper<PlanItem>()
                .eq(PlanItem::getPlanId, planId)
                .orderByAsc(PlanItem::getId));

        PlanDetailVO vo = new PlanDetailVO();
        vo.setPlan(plan);
        vo.setSections(sections);
        vo.setItems(items);

        if (plan.getConsultantId() != null) {
            Employee consultant = employeeMapper.selectById(plan.getConsultantId());
            if (consultant != null) vo.setConsultantName(consultant.getName());
        }
        return vo;
    }
}
