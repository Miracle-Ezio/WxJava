package com.starry.mb.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starry.mb.project.domain.Project;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
