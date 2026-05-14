package com.starry.mb.project;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.web.ApiResponse;
import com.starry.mb.project.domain.Project;
import com.starry.mb.project.dto.ProjectVO;
import com.starry.mb.project.mapper.ProjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectMapper projectMapper;

    public ProjectController(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    /** 列出本租户全部启用项目（小程序选项目下单用）。 */
    @GetMapping
    public ApiResponse<List<ProjectVO>> list() {
        Long tid = PrincipalContext.tenantId();
        List<Project> projects = projectMapper.selectList(new LambdaQueryWrapper<Project>()
                .eq(Project::getTenantId, tid)
                .eq(Project::getStatus, 1)
                .orderByAsc(Project::getCategory)
                .orderByAsc(Project::getId));
        return ApiResponse.ok(projects.stream().map(p -> {
            ProjectVO vo = new ProjectVO();
            vo.setId(p.getId());
            vo.setName(p.getName());
            vo.setCategory(p.getCategory());
            vo.setCoverUrl(p.getCoverUrl());
            vo.setDescription(p.getDescription());
            vo.setDurationMin(p.getDurationMin());
            vo.setUnitPrice(p.getUnitPrice());
            return vo;
        }).toList());
    }
}
