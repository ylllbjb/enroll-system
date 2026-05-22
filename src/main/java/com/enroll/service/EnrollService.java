package com.enroll.service;

import com.enroll.entity.EnrollRecord;
import java.util.List;
import java.util.Map;

/**
 * 选课服务接口
 */
public interface EnrollService {

    /**
     * 处理选课记录：去重 + 排序
     * @param records 原始选课记录列表
     * @return 去重并排序后的列表
     */
    List<EnrollRecord> processRecords(List<EnrollRecord> records);

    /**
     * 批量导入CSV格式的选课数据
     * @param csvText CSV格式文本，每条记录一行
     * @return 处理后的选课记录列表
     */
    List<EnrollRecord> importCsv(String csvText);

    /**
     * 按课程类型分类查询选课记录
     * @param courseType 课程类型（公共课/专业课/选修课）
     * @return 该类型的选课记录列表
     */
    List<EnrollRecord> findByCourseType(String courseType);

    /**
     * 检索选课记录
     * @param keyword 检索关键词（匹配学生ID、课程ID、课程名称、课程类型）
     * @return 匹配的选课记录列表
     */
    List<EnrollRecord> search(String keyword);

    /**
     * 获取所有选课记录
     * @return 全量选课记录
     */
    List<EnrollRecord> getAllRecords();

    /**
     * 按课程类型分组返回所有记录
     * @return 按课程类型分组的选课记录
     */
    Map<String, List<EnrollRecord>> getRecordsGroupedByType();
}
