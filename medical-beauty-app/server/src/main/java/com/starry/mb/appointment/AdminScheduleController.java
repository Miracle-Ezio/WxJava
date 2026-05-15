package com.starry.mb.appointment;

import com.starry.mb.appointment.domain.Appointment;
import com.starry.mb.appointment.domain.AppointmentStatus;
import com.starry.mb.appointment.dto.AppointmentVO;
import com.starry.mb.appointment.mapper.AppointmentMapper;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.exception.BizException;
import com.starry.mb.common.web.ApiResponse;
import com.starry.mb.customer.domain.Customer;
import com.starry.mb.customer.mapper.CustomerMapper;
import com.starry.mb.store.domain.Store;
import com.starry.mb.store.mapper.StoreMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/schedule")
public class AdminScheduleController {

    private final AppointmentMapper appointmentMapper;
    private final StoreMapper storeMapper;
    private final CustomerMapper customerMapper;

    public AdminScheduleController(AppointmentMapper appointmentMapper,
                                   StoreMapper storeMapper,
                                   CustomerMapper customerMapper) {
        this.appointmentMapper = appointmentMapper;
        this.storeMapper = storeMapper;
        this.customerMapper = customerMapper;
    }

    /**
     * 指定门店、日期的全部预约。前端用时间轴渲染。
     * 返回值含门店营业时段，便于前端画时间轴范围。
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> day(
            @RequestParam long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long tid = PrincipalContext.tenantId();
        Store store = storeMapper.selectById(storeId);
        if (store == null || !store.getTenantId().equals(tid)) {
            throw new BizException(40400, "门店不存在");
        }

        List<Appointment> all = appointmentMapper.listActiveOnDay(
                tid, storeId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());

        List<AppointmentVO> rows = all.stream().map(a -> {
            AppointmentVO vo = new AppointmentVO();
            vo.setId(a.getId());
            vo.setStoreId(a.getStoreId());
            vo.setProjectId(a.getProjectId());
            vo.setProjectName(a.getProjectName());
            vo.setUnitPrice(a.getUnitPrice());
            vo.setDurationMin(a.getDurationMin());
            vo.setStartAt(a.getStartAt());
            vo.setEndAt(a.getEndAt());
            vo.setStatus(a.getStatus());
            vo.setStatusLabel(AppointmentStatus.ofCode(a.getStatus()).getLabel());
            vo.setSource(a.getSource());
            vo.setCustomerNote(a.getCustomerNote());
            if (a.getCustomerId() != null) {
                Customer c = customerMapper.selectById(a.getCustomerId());
                if (c != null) vo.setCustomerName(c.getRealName() != null ? c.getRealName() : c.getNickname());
            }
            return vo;
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("date", date.toString());
        result.put("storeId", storeId);
        result.put("storeName", store.getName());
        result.put("capacity", store.getConcurrentCapacity());
        result.put("businessHours", store.getBusinessHours());
        result.put("appointments", rows);
        return ApiResponse.ok(result);
    }
}
