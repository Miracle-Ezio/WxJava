package com.starry.mb.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starry.mb.appointment.domain.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    /**
     * 计算指定门店、时间窗内"占用槽位"的活跃预约数（status IN 1,2,3）。
     * 用于：
     *   - 自助预约提交前的并发校验（在事务内 FOR UPDATE 锁定，防超卖）
     *   - 计算 availability 时无锁版本
     *
     * 两个时间区间 [a, b) 与 [c, d) 重叠 ⇔ a &lt; d AND c &lt; b
     */
    @Select("""
        SELECT COUNT(*) FROM appointment
        WHERE tenant_id = #{tenantId} AND store_id = #{storeId}
          AND deleted_at IS NULL
          AND status IN (1, 2, 3)
          AND start_at < #{endAt}
          AND end_at   > #{startAt}
        ${forUpdate}
        """)
    int countOverlapping(@Param("tenantId") Long tenantId,
                         @Param("storeId")  Long storeId,
                         @Param("startAt")  LocalDateTime startAt,
                         @Param("endAt")    LocalDateTime endAt,
                         @Param("forUpdate") String forUpdateClause);

    /** 列出指定门店、指定一天内的活跃预约（计算 availability 用） */
    @Select("""
        SELECT * FROM appointment
        WHERE tenant_id = #{tenantId} AND store_id = #{storeId}
          AND deleted_at IS NULL
          AND status IN (1, 2, 3)
          AND start_at >= #{dayStart}
          AND start_at <  #{dayEnd}
        ORDER BY start_at
        """)
    List<Appointment> listActiveOnDay(@Param("tenantId") Long tenantId,
                                      @Param("storeId")  Long storeId,
                                      @Param("dayStart") LocalDateTime dayStart,
                                      @Param("dayEnd")   LocalDateTime dayEnd);
}
