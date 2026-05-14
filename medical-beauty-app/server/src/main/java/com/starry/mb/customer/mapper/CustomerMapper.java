package com.starry.mb.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starry.mb.customer.domain.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
