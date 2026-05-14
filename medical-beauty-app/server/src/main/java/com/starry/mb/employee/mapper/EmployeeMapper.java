package com.starry.mb.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starry.mb.employee.domain.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
