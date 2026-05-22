package com.enroll.controller;

import com.enroll.entity.EnrollRecord;
import com.enroll.service.EnrollService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 选课管理 REST API 控制器
 * 【AI生成】提供去重排序、CSV导入、分类查询、检索等API接口
 */
@RestController
@RequestMapping("/api/enroll")
public class EnrollController {

    private final EnrollService enrollService;

    public EnrollController(EnrollService enrollService) {
        this.enrollService = enrollService;
    }

    /**
     * POST /api/enroll/import
     * 批量导入CSV格式选课数据
     */
    @PostMapping("/import")
    public Map<String, Object> importCsv(@RequestBody Map<String, String> body) {
        String csvText = body.getOrDefault("csvData", "");
        if (csvText.isBlank()) {
            return Map.of("success", false, "message", "CSV数据不能为空");
        }
        long start = System.currentTimeMillis();
        List<EnrollRecord> records = enrollService.importCsv(csvText);
        long elapsed = System.currentTimeMillis() - start;

        Map<String, List<EnrollRecord>> grouped = enrollService.getRecordsGroupedByType();
        return Map.of(
                "success", true,
                "message", "导入成功，共处理 " + records.size() + " 条记录，耗时 " + elapsed + "ms",
                "records", records,
                "groupedRecords", grouped
        );
    }

    /**
     * GET /api/enroll/all
     * 获取全部选课记录（经过去重排序的最终结果）
     */
    @GetMapping("/all")
    public List<EnrollRecord> getAll() {
        return enrollService.getAllRecords();
    }

    /**
     * GET /api/enroll/grouped
     * 按课程类型分组返回选课记录
     */
    @GetMapping("/grouped")
    public Map<String, List<EnrollRecord>> getGrouped() {
        return enrollService.getRecordsGroupedByType();
    }

    /**
     * GET /api/enroll/search?keyword=xxx
     * 按关键词检索选课记录
     */
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam("keyword") String keyword) {
        long start = System.currentTimeMillis();
        List<EnrollRecord> records = enrollService.search(keyword);
        long elapsed = System.currentTimeMillis() - start;

        return Map.of(
                "success", true,
                "records", records,
                "count", records.size(),
                "message", records.isEmpty() ? "无匹配选课记录" : "找到 " + records.size() + " 条记录，耗时 " + elapsed + "ms"
        );
    }

    /**
     * GET /api/enroll/type?courseType=专业课
     * 按课程类型查询选课记录
     */
    @GetMapping("/type")
    public List<EnrollRecord> getByType(@RequestParam("courseType") String courseType) {
        return enrollService.findByCourseType(courseType);
    }
}
