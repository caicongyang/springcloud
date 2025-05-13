# Data-Sync 系统文档

## 项目简介

Data-Sync 是一个冗余字段同步系统，主要用于在多个数据库之间同步特定数据字段的值，确保数据一致性。系统通过定义源数据库和目标数据库，以及相应的表和字段，实现自动化的数据同步操作。

## 系统架构图

```mermaid
graph TB
    subgraph 外部服务
        RabbitMQ[(RabbitMQ)]
        XxlJob[(XXL-Job)]
        Apollo[(Apollo配置中心)]
    end
    
    subgraph Data-Sync系统
        WebAPI[Web API接口层]
        Controller[控制器层]
        Service[服务层]
        Manage[管理层]
        JdbcKit[JDBC工具]
        Entity[实体层]
        Mapper[Mapper层]
        DB[(MySQL数据库)]
    end
    
    %% 连接关系
    RabbitMQ -->|消息接收| Controller
    XxlJob -->|定时任务| Controller
    Apollo -->|配置管理| Data-Sync系统
    
    WebAPI --> Controller
    Controller --> Service
    Service --> Manage
    Manage --> JdbcKit
    JdbcKit -->|读写操作| 外部数据源
    
    Service --> Entity
    Service --> Mapper
    Mapper --> DB
    Entity --> DB
    
    subgraph 外部数据源
        SourceDB[(源数据库)]
        TargetDB1[(目标数据库1)]
        TargetDB2[(目标数据库2)]
        TargetDBn[(目标数据库n)]
    end
    
    JdbcKit --> SourceDB
    JdbcKit --> TargetDB1
    JdbcKit --> TargetDB2
    JdbcKit --> TargetDBn
```

## 核心组件

```mermaid
graph LR
    A[Data-Sync系统] --> B[配置管理]
    A --> C[任务管理]
    A --> D[数据同步]
    A --> E[元数据管理]
    
    B --> B1[数据库配置]
    B --> B2[表配置]
    B --> B3[字段配置]
    
    C --> C1[源任务]
    C --> C2[目标任务]
    C --> C3[同步任务数据]
    
    D --> D1[手动同步]
    D --> D2[定时同步]
    D --> D3[消息队列触发同步]
    
    E --> E1[MySQL元数据]
    E --> E2[Oracle元数据]
```

## 数据模型

```mermaid
erDiagram
    SyncDatabase ||--o{ SyncTable : contains
    SyncTable ||--o{ SyncColumn : contains
    SyncTaskSource ||--o{ SyncTaskTarget : has
    SyncTaskSource }|--|| SyncDatabase : uses_source
    SyncTaskSource }|--|| SyncTable : uses_source
    SyncTaskSource }|--|| SyncColumn : uses_source_field
    SyncTaskSource }|--|| SyncColumn : uses_source_query_field
    SyncTaskTarget }|--|| SyncDatabase : uses_target
    SyncTaskTarget }|--|| SyncTable : uses_target
    SyncTaskTarget }|--|| SyncColumn : uses_target_field
    SyncTaskTarget }|--|| SyncColumn : uses_target_query_field
    SyncTaskData }|--|| SyncTaskSource : belongs_to
    
    SyncDatabase {
        int id PK
        string name
        string jdbc_url
    }
    
    SyncTable {
        int id PK
        int database_id FK
        string name
    }
    
    SyncColumn {
        int id PK
        int table_id FK
        string name
        string type
    }
    
    SyncTaskSource {
        int id PK
        string code
        int source_database_id FK
        int source_table_id FK
        int source_field_id FK
        int source_query_field_id FK
    }
    
    SyncTaskTarget {
        int id PK
        int task_source_id FK
        int target_database_id FK
        int target_table_id FK
        int target_field_id FK
        int target_query_field_id FK
    }
    
    SyncTaskData {
        int id PK
        string task_code
        string field_new_data
        string field_old_data
        string query_field_data
        int run_type
    }
```

## 系统流程图

### 数据同步流程

```mermaid
sequenceDiagram
    participant ExternalSystem as 外部系统
    participant MQ as RabbitMQ
    participant Job as XXL-Job
    participant Receiver as SyncDataDirectReceiver
    participant SyncJob as SyncJob
    participant SyncManage as SyncManage
    participant SourceDB as 源数据库
    participant TargetDB as 目标数据库
    
    alt 消息队列触发
        ExternalSystem->>MQ: 发送同步任务消息
        MQ->>Receiver: 接收消息
        Receiver->>Receiver: 记录同步任务数据
    end
    
    alt 定时任务触发
        Job->>SyncJob: 触发定时任务
    end
    
    alt 手动触发
        ExternalSystem->>SyncJob: 触发手动同步
    end
    
    SyncJob->>SyncManage: 调用SyncFieldData()
    SyncManage->>SyncManage: 获取一条待同步数据(runType=0)
    SyncManage->>SyncManage: 更新任务状态为运行中(runType=1)
    SyncManage->>SyncManage: 获取对应的同步任务源配置
    SyncManage->>SourceDB: 查询源数据最新值
    SourceDB-->>SyncManage: 返回最新值
    
    loop 处理每个目标任务
        SyncManage->>TargetDB: 查询目标数据当前值
        TargetDB-->>SyncManage: 返回当前值
        
        alt 值不同
            SyncManage->>TargetDB: 更新目标数据
            TargetDB-->>SyncManage: 更新结果
        end
    end
    
    SyncManage->>SyncManage: 更新任务状态为成功(runType=2)或失败(runType=3)
```

### 任务配置流程

```mermaid
flowchart TD
    A[开始] --> B[配置数据库连接]
    B --> C[配置数据表]
    C --> D[配置数据字段]
    D --> E[创建同步任务源]
    E --> F[创建同步任务目标]
    F --> G[测试数据同步]
    G --> H[配置定时任务]
    H --> I[结束]
```

## 技术栈

```mermaid
graph TB
    subgraph 框架和组件
        Spring[Spring Boot]
        MybatisPlus[MyBatis-Plus]
        RabbitMQ[RabbitMQ]
        XxlJob[XXL-Job]
        Apollo[Apollo配置中心]
    end
    
    subgraph 数据库
        MySQL[MySQL]
        Oracle[Oracle]
    end
    
    subgraph 工具库
        Hutool[Hutool]
        Lombok[Lombok]
    end
```

## 部署架构

```mermaid
graph TB
    subgraph 生产环境
        LB[负载均衡]
        App1[Data-Sync实例1]
        App2[Data-Sync实例2]
        AppN[Data-Sync实例N]
    end
    
    subgraph 外部服务
        RabbitMQ[(RabbitMQ集群)]
        XxlJob[(XXL-Job调度中心)]
        Apollo[(Apollo配置中心)]
        Metrics[(监控系统)]
    end
    
    subgraph 数据库
        MasterDB[(主数据库)]
        SlaveDB[(从数据库)]
    end
    
    LB --> App1
    LB --> App2
    LB --> AppN
    
    App1 --> RabbitMQ
    App2 --> RabbitMQ
    AppN --> RabbitMQ
    
    App1 --> XxlJob
    App2 --> XxlJob
    AppN --> XxlJob
    
    App1 --> Apollo
    App2 --> Apollo
    AppN --> Apollo
    
    App1 --> MasterDB
    App2 --> MasterDB
    AppN --> MasterDB
    
    MasterDB --> SlaveDB
    
    App1 --> Metrics
    App2 --> Metrics
    AppN --> Metrics
```

## 总结

Data-Sync系统是一个用于解决数据冗余同步问题的专用系统，通过配置源数据库和目标数据库之间的映射关系，实现了数据字段的自动化同步。系统支持通过消息队列、定时任务和手动触发三种方式进行数据同步，满足了不同场景下的需求。

数据同步过程的核心是：
1. 从源数据库读取最新值
2. 与目标数据库中的当前值进行比较
3. 如果值不同，则更新目标数据库中的对应字段

系统的设计遵循了分层架构，使用SpringBoot作为基础框架，MyBatis-Plus作为ORM框架，RabbitMQ作为消息队列，XXL-Job作为定时任务调度框架，Apollo作为配置中心。系统支持MySQL和Oracle等不同类型的数据库，通过工厂模式和策略模式实现了对不同数据库的统一操作。 