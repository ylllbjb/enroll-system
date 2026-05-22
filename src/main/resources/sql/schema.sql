-- ========================================
-- 选课管理系统 - MySQL 建表脚本
-- ========================================

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
    course_id VARCHAR(20) PRIMARY KEY COMMENT '课程ID',
    course_name VARCHAR(50) NOT NULL COMMENT '课程名称',
    course_type VARCHAR(20) NOT NULL COMMENT '课程类型（公共课/专业课/选修课）',
    capacity INT DEFAULT 0 COMMENT '课程容量'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 选课记录表
CREATE TABLE IF NOT EXISTS enrollments (
    student_id VARCHAR(20) NOT NULL COMMENT '学生ID',
    course_id VARCHAR(20) NOT NULL COMMENT '课程ID',
    enroll_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    PRIMARY KEY (student_id, course_id),
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 学生表
CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(20) PRIMARY KEY COMMENT '学生ID',
    student_name VARCHAR(50) NOT NULL COMMENT '姓名',
    grade VARCHAR(10) COMMENT '年级',
    major VARCHAR(50) COMMENT '专业'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 选课记录扩展表（存储课程类型，供应用层分类检索使用）
CREATE TABLE IF NOT EXISTS enroll_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL COMMENT '学生ID',
    course_id VARCHAR(20) NOT NULL COMMENT '课程ID',
    course_name VARCHAR(50) NOT NULL COMMENT '课程名称',
    course_type VARCHAR(20) NOT NULL COMMENT '课程类型',
    UNIQUE KEY uk_student_course (student_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
