package com.starry.mb.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.exception.BizException;
import com.starry.mb.common.web.ApiResponse;
import com.starry.mb.customer.domain.Customer;
import com.starry.mb.customer.dto.CustomerDetailVO;
import com.starry.mb.customer.dto.CustomerSummaryVO;
import com.starry.mb.customer.mapper.CustomerMapper;
import com.starry.mb.employee.domain.Employee;
import com.starry.mb.employee.mapper.EmployeeMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final CustomerMapper customerMapper;
    private final EmployeeMapper employeeMapper;

    public AdminCustomerController(CustomerMapper customerMapper, EmployeeMapper employeeMapper) {
        this.customerMapper = customerMapper;
        this.employeeMapper = employeeMapper;
    }

    @GetMapping
    public ApiResponse<List<CustomerSummaryVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String levelCode) {
        Long tid = PrincipalContext.tenantId();
        LambdaQueryWrapper<Customer> q = new LambdaQueryWrapper<Customer>()
                .eq(Customer::getTenantId, tid)
                .orderByDesc(Customer::getLastVisitAt);
        if (StringUtils.isNotBlank(keyword)) {
            q.and(w -> w.like(Customer::getNickname, keyword)
                       .or().like(Customer::getRealName, keyword));
        }
        if (StringUtils.isNotBlank(levelCode)) q.eq(Customer::getLevelCode, levelCode);

        Map<Long, String> consultantNames = consultantNameMap();
        List<Customer> customers = customerMapper.selectList(q);
        return ApiResponse.ok(customers.stream().map(c -> {
            CustomerSummaryVO vo = new CustomerSummaryVO();
            vo.setId(c.getId());
            vo.setNickname(c.getNickname());
            vo.setAvatarUrl(c.getAvatarUrl());
            vo.setRealName(c.getRealName());
            vo.setGender(c.getGender());
            vo.setLevelCode(c.getLevelCode());
            vo.setTotalRecharge(c.getTotalRecharge());
            vo.setBalance(c.getBalance());
            vo.setLastVisitAt(c.getLastVisitAt());
            vo.setFirstVisitAt(c.getFirstVisitAt());
            vo.setConsultantName(consultantNames.get(c.getConsultantId()));
            return vo;
        }).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerDetailVO> detail(@PathVariable long id) {
        Long tid = PrincipalContext.tenantId();
        Customer c = customerMapper.selectById(id);
        if (c == null || c.getDeletedAt() != null || !c.getTenantId().equals(tid)) {
            throw new BizException(40400, "客户不存在");
        }
        CustomerDetailVO vo = new CustomerDetailVO();
        vo.setCustomer(c);
        if (c.getConsultantId() != null) {
            Employee e = employeeMapper.selectById(c.getConsultantId());
            if (e != null) vo.setConsultantName(e.getName());
        }
        return ApiResponse.ok(vo);
    }

    private Map<Long, String> consultantNameMap() {
        Map<Long, String> m = new HashMap<>();
        for (Employee e : employeeMapper.selectList(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getTenantId, PrincipalContext.tenantId()))) {
            m.put(e.getId(), e.getName());
        }
        return m;
    }
}
