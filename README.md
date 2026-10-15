# IntMgr — Java 后端学习案例（对标 10 年工程师的工程范式）

一个刻意写满注释的 **Spring Boot 3 + MyBatis-Plus + Oracle** 教学工程。
目标不是"能跑通"，而是让 Java 初学者看清：**一个真实 10 年老工程师是怎么组织和思考一个后端工程的。**

> 愿景：把分层架构、设计模式、统一响应、全局异常、事务、分页、单元测试这些"看不见的手"，一条条摊开讲给你听。

---

## 一、这个工程教了你什么（大纲）

| 主题 | 落点文件 |
|---|---|
| 启动入口 & 容器扫描 | `Test1Application.java` |
| 分层架构（Controller/Service/Converter） | `controller/`、`service/`、`converter/` |
| 模板方法模式 | `service/impl/AbstractGreetingService.java` |
| 策略模式 + 工厂模式 | `strategy/`、`strategy/GreetingStrategyFactory.java` |
| 建造者模式 | DTO/VO 上的 `@Builder` |
| 统一响应 & 错误码 | `common/response/Result.java`、`ResultCode.java` |
| 全局异常处理 | `common/exception/GlobalExceptionHandler.java` |
| 参数校验 | DTO 上的 `jakarta.validation` 注解 |
| AOP 日志切面 | `aop/ApiLogAspect.java` |
| 数据库持久层（MyBatis-Plus） | `mapper/`、`model/entity/`、`service/impl/UserServiceImpl.java` |
| 事务 | `UserServiceImpl` 上的 `@Transactional` |
| 分页插件 | `config/MybatisPlusConfig.java` |
| 审计字段自动填充 | `config/MyMetaObjectHandler.java` |
| 逻辑删除 | `UserEntity` 上的 `@TableLogic` |
| 单元测试 | `src/test/java/` |

---

## 二、技术栈

- Java 17、Spring Boot 3.5.x
- MyBatis-Plus 3.5.7（持久层，通用 CRUD + 分页）
- Oracle（ojdbc11 驱动 + HikariCP 连接池）
- Lombok、springdoc-openapi（Swagger 文档）、AOP、日志（logback）

---

## 三、目录结构

```
src/main/java/com/example/test1
├── Test1Application.java          # 启动入口
├── controller/                    # 控制层：DemoController(问候) + UserController(用户CRUD)
├── service/                       # 业务层接口
│   └── impl/                      # 业务实现（含模板方法基类）
├── strategy/                      # 策略 + 工厂（问候场景）
│   └── impl/                      # 三个策略实现
├── mapper/                        # 持久层：UserMapper（MyBatis-Plus）
├── model/
│   ├── entity/                    # 数据库实体（UserEntity）
│   ├── dto/                       # 入参对象
│   └── vo/                        # 出参对象
├── converter/                     # DTO/实体/VO 转换
├── enums/                         # 枚举（场景、用户状态）
├── config/                        # 配置类（OpenAPI、MyBatis-Plus、自动填充）
├── aop/                           # 切面（ApiLogAspect）
└── common/
    ├── constant/                  # 常量
    ├── exception/                 # 业务异常 + 全局异常处理器
    └── response/                  # 统一响应 + 错误码

src/main/resources
├── application.yml                # 主配置（含数据源、MyBatis-Plus）
├── application-dev.yml            # 开发环境配置（日志级别）
├── logback-spring.xml             # 日志轮转
└── db/init.sql                    # Oracle 建表脚本
```

---

## 四、快速开始

### 0. 环境要求（重要，先看这里）

- **必须使用 JDK 17 或 21** 构建/运行本工程，pom 里已声明 `<java.version>17</java.version>`。
- **不要用 JDK 25 直接 `mvn` 构建**：Spring Boot 3.5.x 官方只支持到 JDK 24，JDK 25 下
  Lombok 注解处理器不会生成代码，`@Data/@Slf4j/@Builder` 生成的 getter / log / builder 全部丢失，
  编译报一堆"找不到符号"。这是**环境不匹配**，不是代码问题——换回 JDK 17/21 即恢复。
- 本机 `JAVA_HOME` 实测指向 JDK 25.0.3，请再装一个 JDK 17/21，
  并把 IDEA 的项目 SDK 指向它（`Ctrl+Alt+Shift+S` → Project SDK）。

### 1. 准备 Oracle

执行 [src/main/resources/db/init.sql](src/main/resources/db/init.sql) 建表并造 3 条种子数据：

```sql
-- 用任意 Oracle 客户端执行，或用 SQLcl:
@db/init.sql
```

### 2. 改数据库连接

编辑 [application.yml](src/main/resources/application.yml) 里的 `datasource`：

```yaml
datasource:
  url: jdbc:oracle:thin:@//localhost:1521/ORCLPDB1   # 换成你的库（服务名）或 SID 写法
  username: SCOTT
  password: tiger
```

### 3. 启动

```bash
mvn spring-boot:run
```

看到 `Started Test1Application` 即成功。访问 Swagger 文档：<http://localhost:8080/swagger-ui.html>

### 4. 用 curl 调接口

```bash
# 简单问候
curl http://localhost:8080/api/demo/hello

# 个性化问候
curl -X POST http://localhost:8080/api/demo/greet \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","scene":"MORNING"}'

# 新增用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"zhaoliu","nickname":"赵六","email":"zhaoliu@example.com","age":27}'

# 分页查询（第 1 页、每页 5 条、关键字过滤）
curl "http://localhost:8080/api/users?pageNum=1&pageSize=5&keyword=zhang"

# 查询详情
curl http://localhost:8080/api/users/1001

# 更新（只改年龄，其余字段保持不变）
curl -X PUT http://localhost:8080/api/users/1001 \
  -H "Content-Type: application/json" \
  -d '{"age":31}'

# 逻辑删除
curl -X DELETE http://localhost:8080/api/users/1001
```

---

## 五、新手学习路线（建议按顺序读）

1. **先读 [Test1Application.java](src/main/java/com/example/test1/Test1Application.java)**：看懂 Spring Boot 怎么"点火"、扫描哪些类。
2. **读一条最简单的链路**：`DemoController.hello → DemoService → Result`，建立"请求进来→出去"的心理模型。
3. **读分层与设计模式**：`AbstractGreetingService`（模板方法）+ `GreetingStrategyFactory`（策略+工厂）——这是本工程含金量最高的一段。
4. **读通用组件**：`Result`/`ResultCode`/`GlobalExceptionHandler`/`ApiLogAspect`，理解"为什么能统一处理"。
5. **读持久层**：从 `UserEntity → UserMapper → UserServiceImpl → UserController` 走一遍，重点看事务、分页、逻辑删除、自动填充。
6. **读单元测试** `src/test/java`：看老手怎么给策略工厂、模板方法、转换器写测试。
7. **动手改**：加一个策略（如"深夜问候"）、加一个用户字段、加一个查询条件——增量练习巩固。

---

## 六、重要说明：这份注释是"教学刻意为之"

- 每个文件顶部都带 ▌①②③ 的长注释，这是**教学专用**，真实生产代码不会写这么细（维护成本太高）。
- 生产代码的注释原则是"**写 why，不写 what**"——代码已经说了 what，注释解释为什么这么做。
- 请把这份注释当"教材"精读，理解思想；工作里一律精简成 why 级别的短注释。

## 七、它已达线"10 年水平"的点 vs 还需补的点

**已达线：**
- 面向接口编程、开闭原则（策略工厂自动注册，新增场景零改动）
- 统一响应 + 错误码分层 + 全局异常（业务异常返回 200 语义约定）
- 用 `@PostConstruct` + 集合注入替代 if-else 分派；`EnumMap` 做 key
- 审计字段自动填充、逻辑删除、物理分页限流
- 事务 `rollbackFor` 显式声明、厌恶 try-catch 的风格
- 纯单元测试覆盖核心逻辑

**进阶方向（本工程刻意留白，供你继续练）：**
- Flyway / Liquibase 数据库版本管理
- MapStruct 自动映射替代手写 Converter
- Redis 缓存、分布式锁、链路追踪（常量已预留 `X-Trace-Id`）
- 基于 Testcontainers 的数据库集成测试
- 安全（Spring Security / JWT）、幂等、接口限流

---

## 八、测试

```bash
mvn test
```

三个纯单元测试不依赖数据库、不启动 Spring 容器，秒级跑完。