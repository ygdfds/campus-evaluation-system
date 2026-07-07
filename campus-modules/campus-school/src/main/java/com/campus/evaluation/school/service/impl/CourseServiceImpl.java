package com.campus.evaluation.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.school.domain.dto.CourseDTO;
import com.campus.evaluation.school.domain.dto.CourseTeachersDTO;
import com.campus.evaluation.school.domain.entity.*;
import com.campus.evaluation.school.domain.vo.CourseDetailVO;
import com.campus.evaluation.school.domain.vo.CourseVO;
import com.campus.evaluation.school.domain.vo.OptionVO;
import com.campus.evaluation.school.mapper.*;
import com.campus.evaluation.school.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseTeacherMapper courseTeacherMapper;
    private final CourseEnrollmentMapper courseEnrollmentMapper;
    private final TeachingOrgMapper teachingOrgMapper;

    @Override
    public PageResult<CourseVO> list(String keyword, Long teachingOrgId, String status, int pageNum, int pageSize) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getTenantId, tenantId)
                .eq(teachingOrgId != null, Course::getTeachingOrgId, teachingOrgId)
                .like(keyword != null && !keyword.isEmpty(), Course::getCourseName, keyword)
                .orderByAsc(Course::getId);
        Page<Course> page = courseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<CourseVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), records, pageNum, pageSize);
    }

    @Override
    public CourseDetailVO getDetail(Long id) {
        Long tenantId = requireTenantId();
        Course course = getByIdAndTenant(id, tenantId);
        String orgName = getOrgName(tenantId, course.getTeachingOrgId());

        List<CourseTeacher> teachers = courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, id)
                        .eq(CourseTeacher::getTenantId, tenantId)
        );
        List<CourseDetailVO.TeacherInfo> teacherInfos = teachers.stream()
                .map(t -> CourseDetailVO.TeacherInfo.builder()
                        .teacherId(t.getTeacherId())
                        .roleStatus(t.getRoleStatus())
                        .build())
                .collect(Collectors.toList());

        long enrollmentCount = courseEnrollmentMapper.selectCount(
                new LambdaQueryWrapper<CourseEnrollment>()
                        .eq(CourseEnrollment::getCourseId, id)
                        .eq(CourseEnrollment::getTenantId, tenantId)
        );

        return CourseDetailVO.builder()
                .id(course.getId()).tenantId(course.getTenantId()).schoolId(course.getSchoolId())
                .teachingOrgId(course.getTeachingOrgId()).teachingOrgName(orgName)
                .courseCode(course.getCourseCode()).courseName(course.getCourseName())
                .term(course.getTerm()).startAt(course.getStartAt()).endAt(course.getEndAt())
                .teachers(teacherInfos).enrollmentCount((int) enrollmentCount)
                .createdAt(course.getCreatedAt()).updatedAt(course.getUpdatedAt())
                .build();
    }

    @Override
    public CourseVO create(CourseDTO dto) {
        Long tenantId = requireTenantId();
        verifyTeachingOrg(tenantId, dto.getTeachingOrgId());
        checkCourseCodeUnique(tenantId, dto.getCourseCode(), null);

        Course entity = new Course();
        entity.setTenantId(tenantId);
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setTeachingOrgId(dto.getTeachingOrgId());
        entity.setCourseCode(dto.getCourseCode());
        entity.setCourseName(dto.getCourseName());
        entity.setTerm(dto.getTerm());
        entity.setStartAt(dto.getStartAt());
        entity.setEndAt(dto.getEndAt());
        courseMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public CourseVO update(Long id, CourseDTO dto) {
        Long tenantId = requireTenantId();
        Course entity = getByIdAndTenant(id, tenantId);
        verifyTeachingOrg(tenantId, dto.getTeachingOrgId());
        checkCourseCodeUnique(tenantId, dto.getCourseCode(), id);

        entity.setTeachingOrgId(dto.getTeachingOrgId());
        entity.setCourseCode(dto.getCourseCode());
        entity.setCourseName(dto.getCourseName());
        entity.setTerm(dto.getTerm());
        entity.setStartAt(dto.getStartAt());
        entity.setEndAt(dto.getEndAt());
        courseMapper.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void delete(Long id) {
        Long tenantId = requireTenantId();
        getByIdAndTenant(id, tenantId);
        courseMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateTeachers(Long id, CourseTeachersDTO dto) {
        Long tenantId = requireTenantId();
        getByIdAndTenant(id, tenantId);

        // Soft delete existing teachers
        List<CourseTeacher> existing = courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, id)
                        .eq(CourseTeacher::getTenantId, tenantId)
        );
        for (CourseTeacher ct : existing) {
            courseTeacherMapper.deleteById(ct.getId());
        }

        // Insert new teachers
        Long schoolId = SecurityUtils.getSchoolId();
        for (Long teacherId : dto.getTeacherIds()) {
            CourseTeacher ct = new CourseTeacher();
            ct.setTenantId(tenantId);
            ct.setSchoolId(schoolId);
            ct.setCourseId(id);
            ct.setTeacherId(teacherId);
            ct.setRoleStatus("active");
            courseTeacherMapper.insert(ct);
        }
    }

    @Override
    public List<OptionVO> options(Long teachingOrgId) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getTenantId, tenantId)
                .eq(teachingOrgId != null, Course::getTeachingOrgId, teachingOrgId)
                .orderByAsc(Course::getCourseName);
        return courseMapper.selectList(wrapper).stream()
                .map(c -> OptionVO.builder().id(c.getId()).label(c.getCourseName()).value(c.getId()).extra(c.getCourseCode()).build())
                .collect(Collectors.toList());
    }

    private Course getByIdAndTenant(Long id, Long tenantId) {
        Course entity = courseMapper.selectOne(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getId, id)
                        .eq(Course::getTenantId, tenantId)
        );
        if (entity == null) throw new BusinessException(404, "课程不存在");
        return entity;
    }

    private void verifyTeachingOrg(Long tenantId, Long teachingOrgId) {
        TeachingOrgUnit org = teachingOrgMapper.selectOne(
                new LambdaQueryWrapper<TeachingOrgUnit>()
                        .eq(TeachingOrgUnit::getId, teachingOrgId)
                        .eq(TeachingOrgUnit::getTenantId, tenantId)
        );
        if (org == null) throw new BusinessException(404, "教学组织不存在");
    }

    private void checkCourseCodeUnique(Long tenantId, String code, Long excludeId) {
        long count = courseMapper.selectCount(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getTenantId, tenantId)
                        .eq(Course::getCourseCode, code)
                        .ne(excludeId != null, Course::getId, excludeId)
        );
        if (count > 0) throw new BusinessException(409, "课程编码已存在");
    }

    private String getOrgName(Long tenantId, Long teachingOrgId) {
        TeachingOrgUnit org = teachingOrgMapper.selectOne(
                new LambdaQueryWrapper<TeachingOrgUnit>()
                        .eq(TeachingOrgUnit::getId, teachingOrgId)
                        .eq(TeachingOrgUnit::getTenantId, tenantId)
        );
        return org != null ? org.getName() : null;
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "无法获取租户信息");
        return tenantId;
    }

    private CourseVO toVO(Course e) {
        return CourseVO.builder()
                .id(e.getId()).tenantId(e.getTenantId()).schoolId(e.getSchoolId())
                .teachingOrgId(e.getTeachingOrgId())
                .courseCode(e.getCourseCode()).courseName(e.getCourseName())
                .term(e.getTerm()).startAt(e.getStartAt()).endAt(e.getEndAt())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
