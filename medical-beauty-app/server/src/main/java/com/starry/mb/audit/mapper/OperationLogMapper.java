package com.starry.mb.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starry.mb.audit.domain.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
