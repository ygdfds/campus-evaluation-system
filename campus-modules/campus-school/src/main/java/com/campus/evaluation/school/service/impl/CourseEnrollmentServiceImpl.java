package com.campus.evaluation.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.school.domain.dto.CourseEnrollmentDTO;
import com.campus.evaluation.school.domain.entity.ClassGroup;
import com.campus.evaluation.school.domain.entity.Course;
import com.campus.evaluation.school.domain.entity.CourseEnrollment;
import com.campus.evaluation.school.domain.vo.CourseEnrollmentVO;
import com.campus.evaluation.school.mapper.ClassGroupMapper;
import com.campus.evaluation.school.mapper.CourseEnrollmentMapper;
import com.campus.evaluation.school.mapper.CourseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentServiceImpl implements com.campus.evaluation.school.service.CourseEnrollmentService {
    private final CourseEnrollmentMapper enrollmentMapper;
    private final CourseMapper courseMapper;
    private final ClassGroupMapper classGroupMapper;

    @Override
    public PageResult<CourseEnrollmentVO> list(Long courseId, Long studentId, Long classGroupId,
                                                String status, int pageNum, int pageSize) {
        Long tenantId = requireTenantId();
        Page<CourseEnrollment> page = enrollmentMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<CourseEnrollment>()
                        .eq(CourseEnrollment::getTenantId, tenantId)
                        .eq(courseId != null, CourseEnrollment::getCourseId, courseId)
                        .eq(studentId != null, CourseEnrollment::getStudentId, studentId)
                        .eq(classGroupId != null, CourseEnrollment::getClassGroupId, classGroupId)
                        .eq(status != null && !status.isBlank(), CourseEnrollment::getStatus, status)
                        .orderByDesc(CourseEnrollment::getId));
        List<CourseEnrollmentVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), records, pageNum, pageSize);
    }

    @Override
    public CourseEnrollmentVO create(CourseEnrollmentDTO dto) {
        Long tenantId = requireTenantId();
        validateRelations(tenantId, dto);
        long duplicate = enrollmentMapper.selectCount(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getTenantId, tenantId)
                .eq(CourseEnrollment::getCourseId, dto.getCourseId())
                .eq(CourseEnrollment::getStudentId, dto.getStudentId()));
        if (duplicate > 0) throw new BusinessException(409, "Student is already enrolled in this course");

        CourseEnrollment entity = new CourseEnrollment();
        entity.setTenantId(tenantId);
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setCourseId(dto.getCourseId());
        entity.setStudentId(dto.getStudentId());
        entity.setClassGroupId(dto.getClassGroupId());
        entity.setStatus(dto.getStatus() == null ? "active" : dto.getStatus());
        enrollmentMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public CourseEnrollmentVO update(Long id, CourseEnrollmentDTO dto) {
        Long tenantId = requireTenantId();
        CourseEnrollment entity = getById(id, tenantId);
        validateRelations(tenantId, dto);
        long duplicate = enrollmentMapper.selectCount(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getTenantId, tenantId)
                .eq(CourseEnrollment::getCourseId, dto.getCourseId())
                .eq(CourseEnrollment::getStudentId, dto.getStudentId())
                .ne(CourseEnrollment::getId, id));
        if (duplicate > 0) throw new BusinessException(409, "Student is already enrolled in this course");
        entity.setCourseId(dto.getCourseId());
        entity.setStudentId(dto.getStudentId());
        entity.setClassGroupId(dto.getClassGroupId());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        enrollmentMapper.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void delete(Long id) {
        enrollmentMapper.deleteById(getById(id, requireTenantId()).getId());
    }

    private void validateRelations(Long tenantId, CourseEnrollmentDTO dto) {
        Course course = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                .eq(Course::getId, dto.getCourseId())
                .eq(Course::getTenantId, tenantId)
                .eq(Course::getDeleted, 0));
        if (course == null) throw new BusinessException(404, "Course does not exist in the current tenant");
        if (enrollmentMapper.countActiveStudent(dto.getStudentId(), tenantId) == 0) {
            throw new BusinessException(400, "Student must be an active student in the current tenant");
        }
        if (dto.getClassGroupId() != null) {
            ClassGroup group = classGroupMapper.selectOne(new LambdaQueryWrapper<ClassGroup>()
                    .eq(ClassGroup::getId, dto.getClassGroupId())
                    .eq(ClassGroup::getTenantId, tenantId)
                    .eq(ClassGroup::getDeleted, 0));
            if (group == null) throw new BusinessException(404, "Class does not exist in the current tenant");
            if (!course.getTeachingOrgId().equals(group.getTeachingOrgId())) {
                throw new BusinessException(400, "Class and course must belong to the same teaching organization");
            }
        }
    }

    private CourseEnrollment getById(Long id, Long tenantId) {
        CourseEnrollment entity = enrollmentMapper.selectOne(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getId, id)
                .eq(CourseEnrollment::getTenantId, tenantId));
        if (entity == null) throw new BusinessException(404, "Enrollment does not exist");
        return entity;
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "Tenant context is required");
        return tenantId;
    }

    private CourseEnrollmentVO toVO(CourseEnrollment e) {
        return CourseEnrollmentVO.builder()
                .id(e.getId()).tenantId(e.getTenantId()).schoolId(e.getSchoolId())
                .courseId(e.getCourseId()).studentId(e.getStudentId())
                .classGroupId(e.getClassGroupId()).status(e.getStatus())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
    }
}
