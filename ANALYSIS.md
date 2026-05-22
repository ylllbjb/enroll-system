# 题目4：分析及设计

## 一、核心数据模型（ER图）

### 表结构设计

**学生表（students）**
| 字段名 | 数据类型 | 说明 |
|--------|----------|------|
| student_id | VARCHAR(20) | 学生ID（主键），格式S+6位数字 |
| student_name | VARCHAR(50) | 学生姓名 |
| gender | CHAR(1) | 性别（M/F） |
| grade | VARCHAR(10) | 入学年级，如2024 |
| major | VARCHAR(50) | 专业名称 |
| department | VARCHAR(50) | 所属院系 |

**教师表（teachers）**
| 字段名 | 数据类型 | 说明 |
|--------|----------|------|
| teacher_id | VARCHAR(20) | 教师ID（主键），格式T+6位数字 |
| teacher_name | VARCHAR(50) | 教师姓名 |
| title | VARCHAR(20) | 职称（教授/副教授/讲师） |
| department | VARCHAR(50) | 所属院系 |
| email | VARCHAR(100) | 教师邮箱 |

**课程表（courses）**
| 字段名 | 数据类型 | 说明 |
|--------|----------|------|
| course_id | VARCHAR(20) | 课程ID（主键），格式C+6位数字 |
| course_name | VARCHAR(50) | 课程名称 |
| course_type | VARCHAR(20) | 课程类型（公共课/专业课/选修课） |
| capacity | INT | 课程容量上限 |
| enrolled_count | INT | 当前已选人数（冗余字段，用于高性能查询） |
| credit | DECIMAL(3,1) | 学分 |
| teacher_id | VARCHAR(20) | 授课教师ID（外键 → teachers.teacher_id） |
| semester | VARCHAR(10) | 开课学期，如"2024秋" |
| classroom | VARCHAR(50) | 上课教室 |

**选课记录表（enrollments）**
| 字段名 | 数据类型 | 说明 |
|--------|----------|------|
| student_id | VARCHAR(20) | 学生ID（联合主键 + 外键 → students.student_id） |
| course_id | VARCHAR(20) | 课程ID（联合主键 + 外键 → courses.course_id） |
| enroll_time | DATETIME | 选课时间 |
| status | VARCHAR(10) | 选课状态（正常/退课/等待） |

### ER 图（Mermaid格式）

```mermaid
erDiagram
    STUDENTS ||--o{ ENROLLMENTS : "选课"
    COURSES ||--o{ ENROLLMENTS : "被选"
    TEACHERS ||--o{ COURSES : "授课"

    STUDENTS {
        VARCHAR student_id PK "学生ID，S+6位"
        VARCHAR student_name "姓名"
        CHAR gender "性别"
        VARCHAR grade "年级"
        VARCHAR major "专业"
        VARCHAR department "院系"
    }

    TEACHERS {
        VARCHAR teacher_id PK "教师ID，T+6位"
        VARCHAR teacher_name "姓名"
        VARCHAR title "职称"
        VARCHAR department "院系"
        VARCHAR email "邮箱"
    }

    COURSES {
        VARCHAR course_id PK "课程ID，C+6位"
        VARCHAR course_name "课程名称"
        VARCHAR course_type "课程类型"
        INT capacity "课程容量"
        INT enrolled_count "当前已选人数"
        DECIMAL credit "学分"
        VARCHAR teacher_id FK "授课教师ID"
        VARCHAR semester "开课学期"
        VARCHAR classroom "教室"
    }

    ENROLLMENTS {
        VARCHAR student_id PK_FK "学生ID"
        VARCHAR course_id PK_FK "课程ID"
        DATETIME enroll_time "选课时间"
        VARCHAR status "选课状态"
    }
```

### 表间关联关系

| 关联 | 关系类型 | 说明 |
|------|----------|------|
| students ↔ enrollments | 一对多 | 一个学生可以选多门课程 |
| courses ↔ enrollments | 一对多 | 一门课程可以被多名学生选择 |
| teachers ↔ courses | 一对多 | 一名教师可以教授多门课程 |

---

## 二、并发风险分析

### 核心并发问题

选课高峰期（如开学初集中选课时段），大量学生同时抢选同一门热门课程，会产生以下并发问题：

**1. 超卖问题（Lost Update）**
- 场景：课程容量为50人，当前已选49人。学生A和学生B同时点击选课
- 问题：两个事务同时读到 `enrolled_count = 49`，都判断"还有名额"，都成功插入选课记录，最终 `enrolled_count` 变成51，超出容量
- 根本原因：读-判断-写 三个操作不是原子的

**2. 唯一约束冲突**
- 场景：同一学生快速双击选课按钮
- 问题：两个请求同时通过"是否已选"校验，都尝试插入同一条记录，导致唯一约束冲突

### 解决方案：数据库行级锁 + 乐观锁

**方案：悲观锁（数据库行级锁）实现原子选课**

```sql
-- 1. 使用 SELECT ... FOR UPDATE 对课程行加排他锁
SELECT enrolled_count, capacity FROM courses
WHERE course_id = 'C000001' FOR UPDATE;

-- 2. 在应用层判断容量
-- 如果 enrolled_count < capacity，则：
--   a. INSERT INTO enrollments VALUES (...);
--   b. UPDATE courses SET enrolled_count = enrolled_count + 1 WHERE course_id = 'C000001';

-- 3. 事务提交，释放锁
COMMIT;
```

**选课流程：**
1. 开启数据库事务
2. `SELECT ... FOR UPDATE` 锁定课程行（阻止其他事务并发读）
3. 判断 `enrolled_count < capacity`
4. 插入选课记录
5. 更新 `enrolled_count + 1`
6. 提交事务，释放锁

**为什么选择这个方案：**
- 实现简单，Spring `@Transactional` + MyBatis/JPA 即可完成
- 数据库自身保证原子性和隔离性
- 对于选课这种写冲突较高的场景，悲观锁比乐观锁重试成本更低

**备选方案：乐观锁**
- 在 courses 表增加 `version` 字段
- 更新时检查 version，失败则重试
- 适合冲突较少的场景；热点课程可能需要多次重试

---

## 三、索引设计

### enrollments 表索引

| 索引名 | 类型 | 字段 | 设计理由 |
|--------|------|------|----------|
| PRIMARY KEY | 聚簇索引（联合主键） | (student_id, course_id) | 保证同一学生不能重复选同一门课；支持"查询某学生的所有选课" |
| idx_enroll_course | 普通索引 | (course_id) | 支持"统计每门课程选课人数"的 GROUP BY 查询，避免全表扫描 |
| idx_enroll_time | 普通索引 | (enroll_time) | 支持按选课时间范围查询（如查询某时段选课记录） |

### courses 表索引

| 索引名 | 类型 | 字段 | 设计理由 |
|--------|------|------|----------|
| PRIMARY KEY | 聚簇索引（主键） | (course_id) | 按课程ID唯一标识和查询 |
| idx_course_type | 普通索引 | (course_type) | 支持"按课程类型筛选专业课"的 WHERE 条件，过滤选课人数>50的课程 |
| idx_course_type_count | 联合索引 | (course_type, enrolled_count) | **覆盖索引**：题目2的SQL"统计选课人数>50的专业课"可以直接使用此索引完成 WHERE + ORDER BY，避免回表 |

### 设计原则总结

1. **WHERE 条件列优先建索引**：`course_type` 用于筛选专业课
2. **GROUP BY / ORDER BY 列建索引**：`enrolled_count` 用于排序
3. **联合索引遵循最左前缀**：`(course_type, enrolled_count)` 可同时满足"按类型过滤 + 按人数排序"
4. **外键列建索引**：`enrollments.course_id` 加速 JOIN 查询
5. **避免过度索引**：每张表索引控制在3-5个，写入性能可接受
