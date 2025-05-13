# mq-branch-spring-boot-starter

## 概述

`mq-branch-spring-boot-starter` 是一个针对 RocketMQ 消息队列的分支隔离 Spring Boot Starter，主要提供在多分支开发环境中对 RocketMQ 的主题（Topic）和消费组（ConsumerGroup）进行隔离的能力，避免不同分支之间的消息互相干扰。

## 功能特性

- 基于 Git 分支自动为 RocketMQ 的 Topic 和 ConsumerGroup 添加分支后缀
- 支持消息发送时动态选择对应分支的 Topic
- 提供消息分支上下文传递，确保消息处理过程中的分支隔离
- 定时同步 RocketMQ 控制台的消费组列表，以确保消息能被正确路由
- 支持一个Topic多个ConsumerGroup的分支隔离场景

## 核心组件

### 1. 配置类

- `MqBranchRocketMqAutoConfiguration`: Spring Boot 自动配置类，条件注入相关 Bean
- `MqBranchPropertyConstants`: 配置常量定义

### 2. RocketMQ 分支隔离实现

- `MqBranchIsolationBeanPostProcessor`: Bean 后处理器，用于修改 RocketMQ 监听器的 Topic 和 ConsumerGroup，给它们添加分支标识
- `MqBranchRocketMqTemplateAspect`: 切面类，拦截 RocketMQ 消息发送，动态修改 Topic 并在消息中添加分支信息
- `MqBranchRocketMqGroupJob`: 定时任务，每 10 秒从 RocketMQ 控制台获取消费组列表，用于检查分支对应消费组是否存在

### 3. 工具类

- `MqBranchContextHolder`: 基于 ThreadLocal 的分支上下文管理器
- `RocketMqBranchRegexUtil`: 分支名处理工具，确保分支名符合 RocketMQ 的命名规范
- `GitPropertiesCustomerLoader`: Git 属性加载器，读取 Git 信息

### 4. 实体类

- `RocketMqConsumerGroupResponse`: RocketMQ 控制台 API 响应实体类

## 配置项

| 配置项 | 说明 | 默认值 |
| ----- | ---- | ------ |
| mq.branch.enable | 是否启用 MQ 分支隔离 | false |
| mq.branch.type | 分支隔离类型 | - |
| mq.branch.rocketmq.console.url | RocketMQ 控制台 URL | - |
| spring.application.git.generator.path | Git 信息文件路径 | classpath:git.properties |
| git.branch | Git 分支名 | master |

## 工作原理

1. **生产者发送消息**：
   - 通过 AOP 拦截 RocketMQTemplate 的发送方法
   - 根据当前应用的 Git 分支获取分支名
   - 为消息 Topic 添加分支后缀（如 `topic_FEATURE-1233`)
   - 在消息体中添加分支信息

2. **消费者接收消息**：
   - 通过 BeanPostProcessor 在初始化时修改 RocketMQ 监听器的 Topic 和 ConsumerGroup
   - 接收带有分支信息的消息后，通过 ThreadLocal 将分支信息传递到业务处理链路中
   - 消息处理完成后清理线程上下文

3. **分支路由**：
   - 定时从 RocketMQ 控制台获取所有消费组列表
   - 发送消息时检查目标分支对应的消费组是否存在
   - 如果存在则使用该分支的 Topic，否则尝试使用 MASTER 分支的 Topic，再不存在则使用原始 Topic

## ConsumerGroup设计

为了支持一个Topic多个ConsumerGroup的场景，采用了特殊的ConsumerGroup命名策略：

1. **ConsumerGroup命名格式**：
   - 原始消费组名 + "*" + 带分支的Topic名
   - 例如：`originalGroup*topic_FEATURE-1233`
   
2. **优势**：
   - 支持同一个Topic被多个不同的消费组订阅，各自保持独立的分支隔离
   - 在ConsumerGroup中嵌入了它所订阅的Topic信息，使关联关系更加清晰
   - 便于从消费组列表中提取出对应的Topic信息，用于消息路由决策
   - 保留原始消费组标识的同时，添加了分支隔离信息

3. **消息路由逻辑**：
   - 从RocketMQ控制台获取所有消费组列表
   - 从消费组名称中提取出Topic部分（"*"后面的内容）
   - 根据当前要发送的消息的Topic和分支，检查对应的Topic是否存在消费者
   - 按优先级决定使用哪个分支的Topic：当前分支 > MASTER分支 > 原始Topic

## 使用方法

1. 引入依赖：

```xml
<dependency>
    <groupId>com.caicongyang</groupId>
    <artifactId>mq-branch-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

2. 添加配置：

```properties
# 启用 MQ 分支隔离
mq.branch.enable=true
# 设置分支隔离类型为 RocketMQ
mq.branch.type=rocketMQ
# RocketMQ 控制台地址
mq.branch.rocketmq.console.url=http://rocketmq-console:8080
# Git 信息文件路径（可选，默认为 classpath:git.properties）
spring.application.git.generator.path=classpath:git.properties
```

3. 确保项目中配置了 Git 属性插件生成 git.properties 文件：

```xml
<plugin>
    <groupId>pl.project13.maven</groupId>
    <artifactId>git-commit-id-plugin</artifactId>
    <configuration>
        <generateGitPropertiesFile>true</generateGitPropertiesFile>
        <generateGitPropertiesFilename>${project.build.outputDirectory}/git.properties</generateGitPropertiesFilename>
    </configuration>
</plugin>
```

## 注意事项

1. 确保 RocketMQ 控制台可访问且能正确返回消费组信息
2. 分支名会被转换为大写并去除非法字符，确保符合 RocketMQ 的命名规范
3. 在不同分支之间传递消息时，需要显式设置目标分支信息
4. 对于一个Topic多个ConsumerGroup的场景，本库通过特殊的ConsumerGroup命名规则（`originalGroup*topic_BRANCH`）来提供支持 