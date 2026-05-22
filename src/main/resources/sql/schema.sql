-- ========================================
-- 选课管理系统 - 数据库建表脚本
-- 【AI生成】H2 数据库 DDL
-- ========================================

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
    course_id VARCHAR(20) PRIMARY KEY,
    course_name VARCHAR(50) NOT NULL,
    course_type VARCHAR(20) NOT NULL,
    capacity INT DEFAULT 0
);

-- 选课记录表
CREATE TABLE IF NOT EXISTS enrollments (
    student_id VARCHAR(20),
    course_id VARCHAR(20),
    enroll_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, course_id),
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
);

-- 学生表
CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(20) PRIMARY KEY,
    student_name VARCHAR(50) NOT NULL,
    grade VARCHAR(10),
    major VARCHAR(50)
);
