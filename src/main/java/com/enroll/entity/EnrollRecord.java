package com.enroll.entity;

/**
 * 选课记录实体类
 * 【AI生成】基础字段和构造器由AI生成，后续手动补充了 courseType 字段以支持分类功能
 */
public class EnrollRecord {

    /**
     * 学生ID，格式：S+6位数字
     */
    private String studentId;

    /**
     * 课程ID，格式：C+6位数字
     */
    private String courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程类型（公共课/专业课/选修课）
     * 【手动修改】新增字段，用于支持第三题的分类功能
     */
    private String courseType;

    public EnrollRecord() {
    }

    /**
     * 全参构造器（不含课程类型）
     */
    public EnrollRecord(String studentId, String courseId, String courseName) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.courseName = courseName;
    }

    /**
     * 全参构造器（含课程类型）
     * 【手动修改】新增构造器，用于支持CSV导入时传入课程类型
     */
    public EnrollRecord(String studentId, String courseId, String courseName, String courseType) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseType = courseType;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    /**
     * 重写 equals 和 hashCode，用于去重：studentId + courseId 完全一致视为重复
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnrollRecord that = (EnrollRecord) o;
        return studentId.equals(that.studentId) && courseId.equals(that.courseId);
    }

    @Override
    public int hashCode() {
        return 31 * studentId.hashCode() + courseId.hashCode();
    }

    @Override
    public String toString() {
        return String.format("学生ID：%s，课程ID：%s，课程名称：%s", studentId, courseId, courseName);
    }
}
