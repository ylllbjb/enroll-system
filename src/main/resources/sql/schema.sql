-- 选课管理系统 - H2 建表脚本

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
    course_id VARCHAR(20) PRIMARY KEY,
    course_name VARCHAR(50) NOT NULL,
    course_type VARCHAR(20) NOT NULL,
    capacity INT DEFAULT 0
);

-- 选课记录表
CREATE TABLE IF NOT EXISTS enrollments (
    student_id VARCHAR(20) NOT NULL,
    course_id VARCHAR(20) NOT NULL,
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

-- 选课记录扩展表（应用层数据，含课程类型）
CREATE TABLE IF NOT EXISTS enroll_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    course_id VARCHAR(20) NOT NULL,
    course_name VARCHAR(50) NOT NULL,
    course_type VARCHAR(20) NOT NULL,
    CONSTRAINT uk_student_course UNIQUE (student_id, course_id)
);
