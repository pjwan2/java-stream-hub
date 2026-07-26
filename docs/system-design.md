# 系统设计与演进路线

## 当前可运行架构

```mermaid
flowchart LR
    Client[客户端] --> Controller[REST Controller]
    Controller --> Service[业务 Service]
    Service --> UserRepo[UserRepository]
    Service --> FollowRepo[FollowRepository]
    UserRepo --> MySQL[(MySQL)]
    FollowRepo --> MySQL
    Service --> Log[Log4j 2]
```

## 两表 ER 图

```mermaid
erDiagram
    USERS ||--o{ FOLLOWS : "follower"
    USERS ||--o{ FOLLOWS : "followed"
    USERS {
        bigint id PK
        varchar username
        varchar email UK
        varchar role
        timestamp created_at
    }
    FOLLOWS {
        bigint id PK
        bigint follower_id FK
        bigint followed_id FK
        varchar type
        timestamp created_at
    }
```

## 面向图中大规模系统的演进

```mermaid
flowchart LR
    Client --> Gateway[API Gateway]
    Gateway --> UserService[User Service]
    Gateway --> FollowService[Follow Service]
    Gateway --> Recommendation[Recommendation Service]
    UserService --> MySQL[(MySQL)]
    FollowService --> MySQL
    FollowService --> MQ[消息队列]
    MQ --> Aggregator[统计聚合器]
    Aggregator --> Redis[(Redis 排行榜)]
    Recommendation --> Redis
```

学习顺序：

1. 单体两表 CRUD、事务、测试和日志（当前阶段）。
2. Flyway 管理表结构，Testcontainers 做真实数据库集成测试。
3. 分页、索引、JPQL、N+1 与事务并发。
4. Spring Security + JWT，密码必须哈希而不是明文保存。
5. Redis 缓存推荐榜，讨论缓存穿透、击穿和一致性。
6. Kafka/RabbitMQ 异步更新计数，讨论最终一致性和幂等。
7. 最后再拆服务；先理解边界，再承担分布式系统复杂度。
