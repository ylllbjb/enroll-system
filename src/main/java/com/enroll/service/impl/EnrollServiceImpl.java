package com.enroll.service.impl;

import com.enroll.entity.EnrollRecord;
import com.enroll.service.EnrollService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 选课服务实现类
 * 【AI生成】核心业务逻辑基于 JdbcTemplate 操作 MySQL 数据库
 */
@Service
public class EnrollServiceImpl implements EnrollService {

    private final JdbcTemplate jdbcTemplate;

    public EnrollServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<EnrollRecord> processRecords(List<EnrollRecord> records) {
        // 去重：使用 LinkedHashMap 保持顺序，按 studentId + courseId 去重
        Map<String, EnrollRecord> uniqueMap = new LinkedHashMap<>();
        for (EnrollRecord record : records) {
            String key = record.getStudentId() + "_" + record.getCourseId();
            uniqueMap.putIfAbsent(key, record);
        }
        List<EnrollRecord> uniqueList = new ArrayList<>(uniqueMap.values());

        // 排序：先按学生ID升序，再按课程ID升序
        uniqueList.sort(Comparator.comparing(EnrollRecord::getStudentId)
                .thenComparing(EnrollRecord::getCourseId));

        return uniqueList;
    }

    @Override
    @Transactional
    public List<EnrollRecord> importCsv(String csvText) {
        List<EnrollRecord> newRecords = new ArrayList<>();
        // 统一处理 Windows（\r\n）和 Unix（\n）换行符
        String[] lines = csvText.replace("\r\n", "\n").split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split(",");
            if (parts.length >= 3) {
                String studentId = parts[0].trim();
                String courseId = parts[1].trim();
                String courseName = parts[2].trim();
                String courseType = parts.length >= 4 ? parts[3].trim() : "选修课";

                // 自动识别或标准化课程类型
                if (courseType.isEmpty() || (!courseType.equals("公共课")
                        && !courseType.equals("专业课") && !courseType.equals("选修课"))) {
                    courseType = autoDetectCourseType(courseName);
                }

                newRecords.add(new EnrollRecord(studentId, courseId, courseName, courseType));
            }
        }

        // 新数据去重排序
        List<EnrollRecord> dedupedNew = processRecords(newRecords);

        // 批量插入数据库（INSERT IGNORE 处理与已有数据的重复）
        for (EnrollRecord r : dedupedNew) {
            jdbcTemplate.update(
                "INSERT IGNORE INTO enroll_records (student_id, course_id, course_name, course_type) VALUES (?, ?, ?, ?)",
                r.getStudentId(), r.getCourseId(), r.getCourseName(), r.getCourseType()
            );
        }

        return getAllRecords();
    }

    /**
     * 自动检测课程类型
     * 【手动修改】根据课程名称关键词自动识别课程类型，用于适配未标注CSV数据
     */
    private String autoDetectCourseType(String courseName) {
        if (courseName.contains("数学") || courseName.contains("英语")
                || courseName.contains("政治") || courseName.contains("体育")
                || courseName.contains("思修") || courseName.contains("马原")
                || courseName.contains("毛概") || courseName.contains("语文")) {
            return "公共课";
        }
        if (courseName.contains("Java") || courseName.contains("Python")
                || courseName.contains("数据库") || courseName.contains("网络")
                || courseName.contains("数据结构") || courseName.contains("算法")
                || courseName.contains("编译") || courseName.contains("操作")) {
            return "专业课";
        }
        return "选修课";
    }

    @Override
    public List<EnrollRecord> findByCourseType(String courseType) {
        String sql = "SELECT student_id, course_id, course_name, course_type FROM enroll_records WHERE course_type = ? ORDER BY student_id, course_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new EnrollRecord(
                rs.getString("student_id"),
                rs.getString("course_id"),
                rs.getString("course_name"),
                rs.getString("course_type")
        ), courseType);
    }

    @Override
    public List<EnrollRecord> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllRecords();
        }
        String kw = "%" + keyword.trim() + "%";
        String sql = "SELECT student_id, course_id, course_name, course_type FROM enroll_records"
                + " WHERE student_id LIKE ? OR course_id LIKE ? OR course_name LIKE ? OR course_type LIKE ?"
                + " ORDER BY student_id, course_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new EnrollRecord(
                rs.getString("student_id"),
                rs.getString("course_id"),
                rs.getString("course_name"),
                rs.getString("course_type")
        ), kw, kw, kw, kw);
    }

    @Override
    public List<EnrollRecord> getAllRecords() {
        String sql = "SELECT student_id, course_id, course_name, course_type FROM enroll_records ORDER BY student_id, course_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new EnrollRecord(
                rs.getString("student_id"),
                rs.getString("course_id"),
                rs.getString("course_name"),
                rs.getString("course_type")
        ));
    }

    @Override
    public Map<String, List<EnrollRecord>> getRecordsGroupedByType() {
        List<EnrollRecord> all = getAllRecords();
        return all.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCourseType() != null ? r.getCourseType() : "未分类",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }
}
