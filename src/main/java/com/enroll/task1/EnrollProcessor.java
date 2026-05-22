package com.enroll.task1;

import com.enroll.entity.EnrollRecord;
import java.util.*;

/**
 * 题目1：学生选课基础处理工具
 * 【AI生成】核心功能：去重、排序、格式化输出
 *
 * 给AI的提示词：
 * "请编写一个Java选课记录处理工具类EnrollProcessor，接收List<EnrollRecord>作为输入，
 * 需要实现以下功能：
 * 1. 去重规则：studentId + courseId 完全一致视为重复记录，直接移除（与课程名称无关）；
 * 2. 排序规则：先按学生ID升序，学生ID相同时按课程ID升序；
 * 3. 输出要求：返回处理后的列表，同时逐行打印格式化信息：
 *    '学生ID：XXX，课程ID：XXX，课程名称：XXX'
 * 请给出完整可运行代码。"
 */
public class EnrollProcessor {

    /**
     * 处理选课记录：去重 + 排序 + 输出
     */
    public static List<EnrollRecord> process(List<EnrollRecord> input) {
        // 1. 去重：使用LinkedHashMap保持插入顺序，按 studentId + courseId 去重
        Map<String, EnrollRecord> uniqueMap = new LinkedHashMap<>();
        for (EnrollRecord record : input) {
            String key = record.getStudentId() + "_" + record.getCourseId();
            if (!uniqueMap.containsKey(key)) {
                uniqueMap.put(key, record);
            }
        }
        List<EnrollRecord> uniqueList = new ArrayList<>(uniqueMap.values());

        // 2. 排序：先按学生ID升序，再按课程ID升序
        uniqueList.sort(Comparator.comparing(EnrollRecord::getStudentId)
                .thenComparing(EnrollRecord::getCourseId));

        // 3. 逐行打印格式化输出
        System.out.println("========== 处理后的选课记录 ==========");
        for (EnrollRecord record : uniqueList) {
            System.out.println(record);
        }
        System.out.println("共 " + uniqueList.size() + " 条记录");

        return uniqueList;
    }

    /**
     * 主方法 - 验证样例
     */
    public static void main(String[] args) {
        List<EnrollRecord> input = Arrays.asList(
            new EnrollRecord("S000001", "C000001", "Java程序设计"),
            new EnrollRecord("S000002", "C000003", "计算机网络"),
            new EnrollRecord("S000001", "C000001", "Java程序设计"), // 重复记录
            new EnrollRecord("S000001", "C000002", "数据库原理"),
            new EnrollRecord("S000003", "C000001", "Java程序设计")
        );

        System.out.println("原始记录数：" + input.size());
        List<EnrollRecord> result = process(input);
    }
}
