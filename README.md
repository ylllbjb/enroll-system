# 高校选课管理系统

## 使用的 AI 编程工具

**工具名称**：Claude Code（Anthropic Claude 4.x 系列）

## AI 提示词

### 题目1 - Java 基础处理工具

```
请编写一个Java选课记录处理工具类EnrollProcessor，接收List<EnrollRecord>作为输入，需要实现以下功能：
1. 去重规则：studentId + courseId 完全一致视为重复记录，直接移除（与课程名称无关）；
2. 排序规则：先按学生ID升序，学生ID相同时按课程ID升序；
3. 输出要求：返回处理后的列表，同时逐行打印格式化信息：'学生ID：XXX，课程ID：XXX，课程名称：XXX'。
请给出完整可运行代码。
```

### 题目3 - SpringBoot 3.x 全栈开发

```
请使用SpringBoot 3.x（版本3.5.0）、Java 17、Thymeleaf模板引擎，开发一个高校选课管理系统。

【分层设计要求】
严格遵循 Controller → Service → 实体层 架构，禁止业务逻辑写在 Controller 中。

【后端功能需求】
1. 选课记录实体类EnrollRecord（字段：studentId、courseId、courseName、courseType）
2. 去重：studentId + courseId 完全一致视为重复，使用equals/hashCode实现
3. 排序：先按学生ID升序，再按课程ID升序
4. 选课分类：按课程类型（公共课、专业课、选修课）分类存储，支持手动标注和自动识别（根据课程名称关键词）
5. 选课检索：支持按学生ID、课程ID、课程名称、课程类型四种关键词检索，检索不到提示"无匹配选课记录"
6. 性能优化：使用Stream API + Map去重，1000条以上记录检索/排序响应≤1秒，支持单次≥500条批量导入
7. REST API设计：
   - POST /api/enroll/import - 批量CSV导入
   - GET /api/enroll/all - 获取全部选课记录
   - GET /api/enroll/grouped - 按课程类型分组查询
   - GET /api/enroll/search?keyword=xxx - 关键词检索
   - GET /api/enroll/type?courseType=xxx - 按类型查询

【页面设计要求】
1. 单一HTML页面，使用Thymeleaf + 原生HTML/CSS/JavaScript，无需引入复杂前端框架
2. 数据批量导入：提供textarea文本框，用户输入CSV格式选课数据（格式：S000001,C000001,Java程序设计,专业课），点击按钮提交
3. 数据展示：按课程类型分组展示选课数据，无需复杂样式，清晰展示即可
4. 选课检索：搜索框 + 结果表格展示

【前后端衔接】
- 页面加载时通过fetch调用/api/enroll/grouped展示后端样例数据
- CSV文本框数据通过fetch POST提交到/api/enroll/import，后端处理后回显
- 检索结果支持空数据提示

请生成完整可运行的SpringBoot 3.5.0项目代码，包含pom.xml、Application启动类、实体类、Service接口及实现、Controller、application.properties、Thymeleaf前端页面、CSS样式。
```

## 代码标注说明

### AI 生成部分
- `EnrollRecord.java`：实体类基础结构（getter/setter/toString）
- `EnrollServiceImpl.java`：核心业务逻辑（去重、排序、CSV解析、检索）
- `EnrollController.java`：REST API 接口定义
- `PageController.java`：页面路由
- `index.html`：前端页面结构和JavaScript交互逻辑
- `style.css`：全部样式
- `pom.xml`、`application.properties`：项目配置
- `schema.sql`、`data.sql`：数据库脚本
- `task2_queries.sql`：SQL题目答案

### 手动修改/优化部分

| 文件 | 修改内容 | 修改原因 |
|------|----------|----------|
| EnrollRecord.java | 新增 `courseType` 字段及全参构造器 | 适配第三题的分类功能需求 |
| EnrollRecord.java | 重写 `equals/hashCode` | 用于LinkedHashMap去重，题目1核心要求 |
| EnrollServiceImpl.java | 新增 `autoDetectCourseType()` 方法 | 自动识别课程类型，适配CSV未标注数据 |
| index.html | 新增"无匹配选课记录"空结果处理 | 完善检索交互，符合题目"检索不到提示"要求 |
| index.html | 新增 `renderGroupedData()` 空数据判断 | 防止无数据时页面空白 |
| EnrollProcessor.java | 独立Task1+AI提示词+样例main方法 | 体现原始题目要求 |

## 项目结构

```
enroll-system/
├── pom.xml                          # Maven配置（SpringBoot 3.5.0 + Java 17）
├── README.md
├── ANALYSIS.md                      # 题目4：分析设计文档（含ER图）
└── src/main/
    ├── java/com/enroll/
    │   ├── EnrollApplication.java   # SpringBoot启动类
    │   ├── entity/
    │   │   └── EnrollRecord.java    # 选课记录实体类
    │   ├── controller/
    │   │   ├── EnrollController.java # REST API控制器
    │   │   └── PageController.java  # 页面路由控制器
    │   ├── service/
    │   │   ├── EnrollService.java    # 服务接口
    │   │   └── impl/
    │   │       └── EnrollServiceImpl.java # 服务实现
    │   └── task1/
    │       └── EnrollProcessor.java  # 题目1：独立处理工具
    └── resources/
        ├── application.properties    # 应用配置
        ├── static/css/style.css      # 样式表
        ├── templates/index.html      # Thymeleaf前端页面
        └── sql/
            ├── schema.sql            # 建表脚本
            ├── data.sql              # 测试数据
            └── task2_queries.sql     # 题目2：SQL答案
```

## 运行方式

```bash
# 1. 确保已安装 JDK 17+ 和 Maven 3.6+

# 2. 进入项目目录
cd enroll-system

# 3. 启动项目
mvn spring-boot:run

# 4. 打开浏览器访问
http://localhost:8080
```

## 功能验证

1. **页面加载**：自动展示后端写死的样例选课数据（按课程类型分组）
2. **CSV导入**：在文本框中粘贴CSV数据，点击"导入数据"，数据经后端处理后回显
3. **选课检索**：输入关键词（学生ID/课程ID/课程名称/课程类型），查看检索结果
4. **空结果提示**：输入不存在的关键词，显示"无匹配选课记录"
5. **H2控制台**：访问 http://localhost:8080/h2-console 查看SQL题目相关表数据
