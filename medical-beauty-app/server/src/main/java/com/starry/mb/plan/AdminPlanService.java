package com.starry.mb.plan;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starry.mb.audit.AuditLogger;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.exception.BizException;
import com.starry.mb.customer.domain.Customer;
import com.starry.mb.customer.mapper.CustomerMapper;
import com.starry.mb.plan.domain.Plan;
import com.starry.mb.plan.domain.PlanItem;
import com.starry.mb.plan.domain.PlanSection;
import com.starry.mb.plan.dto.PlanDetailVO;
import com.starry.mb.plan.dto.PlanUpsertRequest;
import com.starry.mb.plan.mapper.PlanItemMapper;
import com.starry.mb.plan.mapper.PlanMapper;
import com.starry.mb.plan.mapper.PlanSectionMapper;
import com.starry.mb.project.domain.Project;
import com.starry.mb.project.mapper.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminPlanService {

    private final PlanMapper planMapper;
    private final PlanSectionMapper sectionMapper;
    private final PlanItemMapper itemMapper;
    private final CustomerMapper customerMapper;
    private final ProjectMapper projectMapper;
    private final AuditLogger audit;
    private final com.starry.mb.notify.WxSubscribeService wxSubscribe;

    public AdminPlanService(PlanMapper planMapper,
                            PlanSectionMapper sectionMapper,
                            PlanItemMapper itemMapper,
                            CustomerMapper customerMapper,
                            ProjectMapper projectMapper,
                            AuditLogger audit,
                            com.starry.mb.notify.WxSubscribeService wxSubscribe) {
        this.planMapper = planMapper;
        this.sectionMapper = sectionMapper;
        this.itemMapper = itemMapper;
        this.customerMapper = customerMapper;
        this.projectMapper = projectMapper;
        this.audit = audit;
        this.wxSubscribe = wxSubscribe;
    }

    /** 列出某客户全部方案（含草稿，员工可见）。 */
    public List<Plan> listByCustomer(long customerId) {
        Long tid = PrincipalContext.tenantId();
        ensureCustomerInTenant(customerId, tid);
        return planMapper.selectList(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getTenantId, tid)
                .eq(Plan::getCustomerId, customerId)
                .orderByDesc(Plan::getId));
    }

    public PlanDetailVO detail(long id) {
        Long tid = PrincipalContext.tenantId();
        Plan plan = planMapper.selectById(id);
        if (plan == null || plan.getDeletedAt() != null || !plan.getTenantId().equals(tid)) {
            throw new BizException(40400, "规划方案不存在");
        }
        PlanDetailVO vo = new PlanDetailVO();
        vo.setPlan(plan);
        vo.setSections(sectionMapper.selectList(new LambdaQueryWrapper<PlanSection>()
                .eq(PlanSection::getPlanId, id)
                .orderByAsc(PlanSection::getSort)));
        vo.setItems(itemMapper.selectList(new LambdaQueryWrapper<PlanItem>()
                .eq(PlanItem::getPlanId, id)
                .orderByAsc(PlanItem::getId)));
        return vo;
    }

    @Transactional
    public Plan create(PlanUpsertRequest req) {
        Long tid = PrincipalContext.tenantId();
        Customer c = ensureCustomerInTenant(req.getCustomerId(), tid);

        Plan p = new Plan();
        applyPlanFields(p, req, c);
        p.setStatus(1);            // 草稿
        p.setVersion(1);
        p.setConsultantId(PrincipalContext.employeeId());
        planMapper.insert(p);

        replaceSectionsAndItems(p.getId(), tid, req);
        audit.record("plan", "create", p.getId(),
                "新建规划方案：" + p.getTitle() + " · 客户 #" + p.getCustomerId());
        return p;
    }

    @Transactional
    public Plan update(long id, PlanUpsertRequest req) {
        Long tid = PrincipalContext.tenantId();
        Plan p = planMapper.selectById(id);
        if (p == null || p.getDeletedAt() != null || !p.getTenantId().equals(tid)) {
            throw new BizException(40400, "规划方案不存在");
        }
        applyPlanFields(p, req, null);
        planMapper.updateById(p);
        replaceSectionsAndItems(id, tid, req);
        audit.record("plan", "update", id, "更新规划方案：" + p.getTitle());
        return p;
    }

    /** 推送方案给客户（草稿 → 已推送）。 */
    @Transactional
    public Plan push(long id) {
        Long tid = PrincipalContext.tenantId();
        Plan p = planMapper.selectById(id);
        if (p == null || !p.getTenantId().equals(tid)) {
            throw new BizException(40400, "规划方案不存在");
        }
        if (p.getStatus() >= 2) {
            throw new BizException(40010, "已推送过，不能重复推送");
        }
        p.setStatus(2);
        p.setPushedAt(LocalDateTime.now());
        planMapper.updateById(p);
        audit.record("plan", "push", id, "推送规划方案给客户：" + p.getTitle());
        wxSubscribe.notifyPlanPushed(p.getId(), p.getCustomerId(), p.getTitle());
        return p;
    }

    @Transactional
    public void softDelete(long id) {
        Long tid = PrincipalContext.tenantId();
        Plan p = planMapper.selectById(id);
        if (p == null || !p.getTenantId().equals(tid)) {
            throw new BizException(40400, "规划方案不存在");
        }
        planMapper.deleteById(id);
        audit.record("plan", "delete", id, "删除规划方案：" + p.getTitle());
    }

    // ─────────────────────────────────────────────────────────

    private void applyPlanFields(Plan p, PlanUpsertRequest req, Customer customer) {
        if (customer != null) p.setCustomerId(customer.getId());
        p.setTitle(req.getTitle());
        p.setSubtitle(req.getSubtitle());
        p.setCoverUrl(req.getCoverUrl());
        p.setValidFrom(req.getValidFrom());
        p.setValidTo(req.getValidTo());
        p.setTotalPrice(req.getTotalPrice());
        p.setDiscountPrice(req.getDiscountPrice());
        p.setAnalysisText(req.getAnalysisText());
    }

    /** 简单粗暴：删除旧 sections / items，整体重写。 */
    private void replaceSectionsAndItems(Long planId, Long tid, PlanUpsertRequest req) {
        sectionMapper.delete(new LambdaQueryWrapper<PlanSection>()
                .eq(PlanSection::getPlanId, planId));
        itemMapper.delete(new LambdaQueryWrapper<PlanItem>()
                .eq(PlanItem::getPlanId, planId));

        if (req.getSections() != null) {
            int sortIdx = 0;
            for (PlanUpsertRequest.SectionDTO s : req.getSections()) {
                PlanSection sec = new PlanSection();
                sec.setPlanId(planId);
                sec.setSort(s.getSort() != null ? s.getSort() : sortIdx++);
                sec.setType(s.getType());
                sec.setTitle(s.getTitle());
                sec.setContent(s.getContent());
                sectionMapper.insert(sec);
            }
        }
        if (req.getItems() != null) {
            for (PlanUpsertRequest.ItemDTO it : req.getItems()) {
                if (it.getProjectId() == null) continue;
                Project proj = projectMapper.selectById(it.getProjectId());
                if (proj == null || !proj.getTenantId().equals(tid)) {
                    throw new BizException(40000, "项目不存在或越权: " + it.getProjectId());
                }
                PlanItem item = new PlanItem();
                item.setPlanId(planId);
                item.setProjectId(it.getProjectId());
                item.setProjectName(it.getProjectName() != null ? it.getProjectName() : proj.getName());
                item.setPlannedCount(it.getPlannedCount() != null ? it.getPlannedCount() : 1);
                item.setDoneCount(0);
                item.setUnitPrice(it.getUnitPrice() != null ? it.getUnitPrice() : proj.getUnitPrice());
                item.setActivityPrice(it.getActivityPrice());
                item.setTotalPrice(it.getTotalPrice());
                item.setStatus(1);
                item.setNotes(it.getNotes());
                itemMapper.insert(item);
            }
        }
    }

    private Customer ensureCustomerInTenant(Long customerId, Long tid) {
        Customer c = customerMapper.selectById(customerId);
        if (c == null || c.getDeletedAt() != null || !c.getTenantId().equals(tid)) {
            throw new BizException(40400, "客户不存在");
        }
        return c;
    }
}
