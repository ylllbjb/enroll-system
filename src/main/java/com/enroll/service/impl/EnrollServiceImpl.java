package com.enroll.service.impl;

import com.enroll.entity.EnrollRecord;
import com.enroll.service.EnrollService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 选课服务实现类
 * 【AI生成】核心业务逻辑：去重、排序、分类、检索、批量导入
 */
@Service
public class EnrollServiceImpl implements EnrollService {

    // 使用线程安全的 ConcurrentHashMap 存储选课记录（模拟数据库）
    private final List<EnrollRecord> storage = Collections.synchronizedList(new ArrayList<>());

    // 样例数据初始化
    public EnrollServiceImpl() {
        initSampleData();
    }

    /**
     * 初始化样例数据
     */
    private void initSampleData() {
        storage.add(new EnrollRecord("S000001", "C000001", "Java程序设计", "专业课"));
        storage.add(new EnrollRecord("S000002", "C000003", "计算机网络", "专业课"));
        storage.add(new EnrollRecord("S000001", "C000001", "Java程序设计", "专业课")); // 重复记录
        storage.add(new EnrollRecord("S000001", "C000002", "数据库原理", "专业课"));
        storage.add(new EnrollRecord("S000003", "C000001", "Java程序设计", "专业课"));
        storage.add(new EnrollRecord("S000004", "C000004", "高等数学", "公共课"));
        storage.add(new EnrollRecord("S000005", "C000005", "大学英语", "公共课"));
        storage.add(new EnrollRecord("S000004", "C000006", "音乐鉴赏", "选修课"));
        storage.add(new EnrollRecord("S000006", "C000007", "Python基础", "选修课"));
        storage.add(new EnrollRecord("S000002", "C000008", "数据结构", "专业课"));
        // 去重初始化
        List<EnrollRecord> deduped = processRecords(new ArrayList<>(storage));
        storage.clear();
        storage.addAll(deduped);
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
    public List<EnrollRecord> importCsv(String csvText) {
        List<EnrollRecord> newRecords = new ArrayList<>();
        String[] lines = csvText.split("\\n");

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

                // 自动识别课程类型：如果未标注或标注不标准，尝试识别
                if (courseType.isEmpty() || (!courseType.equals("公共课") && !courseType.equals("专业课") && !courseType.equals("选修课"))) {
                    courseType = autoDetectCourseType(courseName);
                }

                newRecords.add(new EnrollRecord(studentId, courseId, courseName, courseType));
            }
        }

        // 合并已有数据 + 新数据，然后去重排序
        List<EnrollRecord> allRecords = new ArrayList<>(storage);
        allRecords.addAll(newRecords);
        List<EnrollRecord> processed = processRecords(allRecords);

        // 更新存储
        storage.clear();
        storage.addAll(processed);

        return processed;
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
        return storage.stream()
                .filter(r -> courseType.equals(r.getCourseType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollRecord> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>(storage);
        }
        String kw = keyword.trim();
        return storage.stream()
                .filter(r -> {
                    if (r.getStudentId().contains(kw)) return true;
                    if (r.getCourseId().contains(kw)) return true;
                    if (r.getCourseName() != null && r.getCourseName().contains(kw)) return true;
                    if (r.getCourseType() != null && r.getCourseType().contains(kw)) return true;
                    return false;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollRecord> getAllRecords() {
        return new ArrayList<>(storage);
    }

    @Override
    public Map<String, List<EnrollRecord>> getRecordsGroupedByType() {
        return storage.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCourseType() != null ? r.getCourseType() : "未分类",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }
}
