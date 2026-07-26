# Stream Hub：用项目学习 Java / Spring Boot

这是一个两表的直播关注与推荐 Web Service，目标是边做项目边掌握 Java
知识点和面试点。

## 技术栈

- Java 21
- Spring Boot 4 + Spring Web
- Hibernate / Spring Data JPA
- MySQL 8.4（Docker Compose）
- Maven
- Log4j 2
- JUnit 5 + Mockito

## 第一次运行

```bash
docker compose up -d
mvn test
mvn spring-boot:run
```

服务启动后，可在另一个终端执行：

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"Alice","email":"alice@example.com","role":"VIEWER"}'

curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"Bob","email":"bob@example.com","role":"STREAMER"}'

curl -X POST http://localhost:8080/api/follows \
  -H "Content-Type: application/json" \
  -d '{"followerId":1,"followedId":2,"type":"FOLLOW"}'

curl http://localhost:8080/api/recommendations?limit=10
```

Windows PowerShell 也可以用 `curl.exe` 执行以上命令。

## API

| 方法 | 路径 | 用途 |
|---|---|---|
| POST | `/api/users` | 注册用户或主播 |
| GET | `/api/users` | 查询全部用户 |
| GET | `/api/users/{id}` | 查询一个用户 |
| PUT | `/api/users/{id}` | 修改用户名/角色 |
| DELETE | `/api/users/{id}` | 删除用户 |
| POST | `/api/follows` | 关注或订阅主播 |
| GET | `/api/users/{id}/follows` | 查询用户的关注 |
| DELETE | `/api/follows/{id}` | 取消关注/订阅 |
| GET | `/api/recommendations?limit=10` | 按关注数推荐主播 |

## 学习入口

先阅读 [第一课](docs/lesson-01.md)，再结合代码运行测试。架构与演进路线见
[系统设计](docs/system-design.md)。
