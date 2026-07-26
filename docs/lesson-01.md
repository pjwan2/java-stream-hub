# 第一课：从需求到两张表

## 1. 为什么选择 `users` 和 `follows`

`users` 保存“人”，`follows` 保存“人与人之间的关系”。一个用户可以关注多个
主播，一个主播也可以被多个用户关注，因此它本质上是一个多对多关系。

我们没有在 `users` 里存一个逗号分隔的关注 ID 列表，因为那会破坏关系数据库
的范式，难以建立外键、去重、统计和分页。

`follows` 的三个核心字段：

- `follower_id`：发起关注的人
- `followed_id`：被关注的主播
- `type`：`FOLLOW` 或 `SUBSCRIBE`

三列组成唯一约束，数据库会成为并发场景下防止重复数据的最后一道防线。

## 2. 今天代码里的 Java 知识

### `enum`

`UserRole`、`FollowType` 用枚举表达有限状态，比魔法字符串安全。JPA 使用
`EnumType.STRING`，数据库里保存可读文本；不要轻易使用 `ORDINAL`，因为调整
枚举顺序会让历史数据含义改变。

### `record`

请求和响应 DTO 使用 Java `record`。它适合不可变的数据载体，编译器自动生成
构造器、访问器、`equals`、`hashCode` 和 `toString`。

### 构造器注入

Service 通过构造器接收 Repository。依赖显式、对象可测试，也能将字段声明为
`final`。面试中通常优于字段上的 `@Autowired`。

### Stream API

`findAll().stream().map(...).toList()` 展示了“集合 → 流 → 映射 → 新集合”的
处理方式。Stream 更适合表达转换，不要用它强行替代所有普通循环。

## 3. 今天代码里的 Spring/JPA 知识

- `@RestController`：接收 HTTP 请求并把返回值序列化为 JSON。
- `@Service`：承载业务规则和事务边界。
- `JpaRepository`：提供通用 CRUD，并能根据方法名派生 SQL。
- `@Entity`：Java 对象映射为数据库表。
- `@ManyToOne(fetch = LAZY)`：多个关注记录可指向同一个用户。
- `@Transactional`：一组数据库操作要么一起成功，要么一起失败。
- `open-in-view: false`：避免在 Controller 层意外触发懒加载和 N+1 查询。

## 4. JUnit 单元测试怎么看

`@ExtendWith(MockitoExtension.class)` 启用 Mockito，`@Mock` 创建假的 Repository。
测试只验证 Service 的业务逻辑，不启动 Spring、不连接 MySQL，所以速度快。

建议亲手完成这个小练习：

1. 给 `UserServiceTest` 增加“查询不存在的用户应抛出 `NotFoundException`”。
2. 先写测试并看到失败（红）。
3. 再检查实现或补实现让它通过（绿）。

这就是 TDD 最小循环：Red → Green → Refactor。

## 5. 第一轮面试题

1. Entity 为什么需要无参构造器？
2. `LAZY` 与 `EAGER` 的区别是什么？
3. DTO 和 Entity 为什么不直接共用？
4. 唯一约束和代码里的 `exists...` 检查为什么都需要？
5. `@Transactional(readOnly = true)` 有什么作用？

先用自己的话回答，再查资料。下一课会实现 Repository 集成测试和数据库迁移。
