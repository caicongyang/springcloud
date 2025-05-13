# XXL-Job 环境隔离实现指南

## 背景介绍

在微服务架构中，我们经常需要同一个应用的不同版本并行运行（如开发环境、测试环境、生产环境），或者同一环境中运行不同分支版本的应用。使用XXL-Job作为任务调度平台时，如何确保任务只在指定版本/环境的执行器上运行，避免跨环境调度导致的问题？本文将介绍如何实现XXL-Job的环境隔离功能。

## 环境隔离的核心思路

环境隔离的核心是为执行器和任务引入"版本"标识，确保任务只被分发到相同版本的执行器上执行。这种隔离机制允许同一个调度中心管理多个环境的执行器，显著提高资源利用率。

## 实现步骤

### 1. 数据库表结构调整

首先需要对数据库表结构进行调整，主要涉及以下表：

```sql
-- 在 xxl_job_info 表中增加 version 字段
ALTER TABLE xxl_job_info ADD COLUMN version VARCHAR(50) DEFAULT 'default';

-- 在 xxl_job_registry 表中增加 registry_version 字段
ALTER TABLE xxl_job_registry ADD COLUMN registry_version VARCHAR(50) DEFAULT 'default';
```

### 2. 模型类修改

#### 2.1 任务信息类修改

在`XxlJobInfo`类中添加版本字段：

```java
public class XxlJobInfo {
    // 现有字段...
    
    // 新增版本字段
    private String version;
    
    // getter 和 setter 方法
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
}
```

#### 2.2 执行器注册参数修改

在`RegistryParam`类中添加版本字段：

```java
public class RegistryParam implements Serializable {
    // 现有字段...
    private String registryGroup;
    private String registryKey;
    private String registryValue;
    
    // 新增版本字段
    private String registryVersion;
    
    // 构造函数更新
    public RegistryParam(String registryGroup, String registryKey, String registryValue, String registryVersion) {
        this.registryGroup = registryGroup;
        this.registryKey = registryKey;
        this.registryValue = registryValue;
        this.registryVersion = registryVersion;
    }
    
    // getter 和 setter 方法
    public String getRegistryVersion() {
        return registryVersion;
    }
    
    public void setRegistryVersion(String registryVersion) {
        this.registryVersion = registryVersion;
    }
}
```

#### 2.3 注册表模型修改

在`XxlJobRegistry`类中添加版本字段：

```java
public class XxlJobRegistry {
    // 现有字段...
    
    // 新增版本字段
    private String registryVersion;
    
    // getter 和 setter 方法
    public String getRegistryVersion() {
        return registryVersion;
    }
    
    public void setRegistryVersion(String registryVersion) {
        this.registryVersion = registryVersion;
    }
}
```

#### 2.4 新增版本相关类

创建版本枚举类，定义常用版本标识：

```java
public enum VersionEnum {
    DEFAULT("default"),
    MASTER("master");
    // 可根据需要添加其他环境
    
    private String version;
    
    VersionEnum(String version) {
        this.version = version;
    }
    
    public String get() {
        return version;
    }
}
```

创建版本分隔符接口：

```java
public interface VersionSeparator {
    String VSP = "@VSP@";  // 版本分隔符常量
}
```

### 3. 执行器端改造

#### 3.1 `XxlJobExecutor`类修改

在`XxlJobExecutor`类中添加版本字段并修改相关方法：

```java
public class XxlJobExecutor {
    // 现有字段...
    private String adminAddresses;
    private String accessToken;
    private String appname;
    private String address;
    private String ip;
    private int port;
    private String logPath;
    private int logRetentionDays;
    
    // 新增版本字段
    private String version;
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    // 修改启动方法
    public void start() throws Exception {
        // ... 现有代码 ...
        
        // 初始化嵌入式服务器，传递版本信息
        initEmbedServer(address, ip, port, appname, accessToken, version);
    }
    
    // 修改嵌入式服务器初始化方法
    private void initEmbedServer(String address, String ip, int port, String appname, String accessToken, String version) throws Exception {
        // ... 现有代码 ...
        
        // 启动服务器，传递版本信息
        embedServer = new EmbedServer();
        embedServer.start(address, port, appname, accessToken, version);
    }
}
```

#### 3.2 `EmbedServer`类修改

修改`EmbedServer`类，在启动时接收并传递版本信息：

```java
public class EmbedServer {
    // ... 现有代码 ...
    
    // 修改启动方法，增加版本参数
    public void start(final String address, final int port, final String appname, final String accessToken, String version) {
        // ... 现有代码 ...
        
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // ... 线程启动代码 ...
                
                try {
                    // ... 服务器启动代码 ...
                    
                    // 启动注册，传递版本信息
                    startRegistry(appname, address, version);
                    
                    // ... 其他代码 ...
                } catch (Exception e) {
                    // ... 异常处理 ...
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    // 修改注册启动方法
    public void startRegistry(final String appname, final String address, String version) {
        // 启动注册线程，传递版本信息
        ExecutorRegistryThread.getInstance().start(appname, address, version);
    }
}
```

#### 3.3 `ExecutorRegistryThread`类修改

修改`ExecutorRegistryThread`类，在注册和注销时传递版本信息：

```java
public class ExecutorRegistryThread {
    // ... 现有代码 ...
    
    // 修改启动方法，增加版本参数
    public void start(final String appname, final String address, String version) {
        // ... 参数检查 ...
        
        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 注册过程
                while (!toStop) {
                    try {
                        // 创建注册参数，包含版本信息
                        RegistryParam registryParam = new RegistryParam(
                            RegistryConfig.RegistType.EXECUTOR.name(), 
                            appname, 
                            address,
                            version
                        );
                        
                        // 向所有管理节点发送注册请求
                        for (AdminBiz adminBiz: XxlJobExecutor.getAdminBizList()) {
                            try {
                                ReturnT<String> registryResult = adminBiz.registry(registryParam);
                                // ... 处理结果 ...
                            } catch (Exception e) {
                                // ... 异常处理 ...
                            }
                        }
                    } catch (Exception e) {
                        // ... 异常处理 ...
                    }
                    
                    // ... 休眠逻辑 ...
                }
                
                // 注册移除
                try {
                    // 创建注册参数，包含版本信息
                    RegistryParam registryParam = new RegistryParam(
                        RegistryConfig.RegistType.EXECUTOR.name(), 
                        appname, 
                        address,
                        version
                    );
                    
                    // 向所有管理节点发送注销请求
                    for (AdminBiz adminBiz: XxlJobExecutor.getAdminBizList()) {
                        // ... 注销逻辑 ...
                    }
                } catch (Exception e) {
                    // ... 异常处理 ...
                }
            }
        });
        
        registryThread.setDaemon(true);
        registryThread.start();
    }
}
```

### 4. 调度中心端改造

核心是修改`XxlJobTrigger`类的触发逻辑，增加版本过滤：

```java
public static void trigger(int jobId, TriggerTypeEnum triggerType, int failRetryCount, 
                          String executorShardingParam, String executorParam,
                          String addressList, String version) {
    // 加载任务信息
    XxlJobInfo jobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(jobId);
    
    // 处理执行参数
    if (executorParam != null) {
        jobInfo.setExecutorParam(executorParam);
    }
    
    // 设置版本信息
    if (version != null) {
        jobInfo.setVersion(version);
    }
    
    // 获取执行器组信息
    XxlJobGroup group = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().load(jobInfo.getJobGroup());
    
    // 版本过滤：在非默认版本下，只选择相同版本的执行器
    if (!StringUtils.isEmpty(jobInfo.getVersion()) && 
        !VersionEnum.DEFAULT.get().equals(jobInfo.getVersion())) {
        // 查询该版本下的所有执行器
        List<XxlJobRegistry> registryList = XxlJobAdminConfig.getAdminConfig()
                .getXxlJobRegistryDao()
                .findByVersion(jobInfo.getVersion(), group.getRegistryList());
        
        // 提取地址列表
        List<String> addresses = new ArrayList<>();
        for (XxlJobRegistry registry : registryList) {
            addresses.add(registry.getRegistryValue());
        }
        
        // 更新执行器地址列表
        if (!addresses.isEmpty()) {
            group.setAddressList(String.join(",", addresses));
        }
    }
    
    // 继续任务调度逻辑...
}
```

### 5. Mapper XML 文件修改

修改相关的XML映射文件，添加对应的字段和查询方法：

```xml
<!-- 在 XxlJobInfoMapper.xml 中添加版本字段映射 -->
<resultMap id="XxlJobInfo" type="com.xxl.job.admin.core.model.XxlJobInfo">
    <!-- 现有字段 -->
    <result column="version" property="version" />
</resultMap>

<!-- 在 XxlJobRegistryMapper.xml 中添加版本字段映射 -->
<resultMap id="XxlJobRegistry" type="com.xxl.job.admin.core.model.XxlJobRegistry">
    <!-- 现有字段 -->
    <result column="registry_version" property="registryVersion" />
</resultMap>

<!-- 在 XxlJobRegistryMapper.xml 中添加版本相关查询 -->
<select id="findByVersion" parameterType="java.util.HashMap" resultMap="XxlJobRegistry">
    SELECT <include refid="Base_Column_List" />
    FROM xxl_job_registry AS t
    WHERE t.registry_version = #{registryVersion}
    <if test="list != null and list.size() > 0">
        and registry_value in
        <foreach collection="list" separator="," item="item" open="(" close=")">
            #{item}
        </foreach>
    </if>
</select>
```

### 6. UI 界面修改

在任务配置界面中添加版本选择字段，让用户可以指定任务的版本：

- 修改`jobinfo.index.ftl`模板，添加版本选择下拉框
- 修改`jobinfo.index.1.js`脚本，处理版本选择逻辑

## 注意事项

1. 确保数据库表结构调整向后兼容，默认版本为"default"
2. 版本标识应该保持统一，建议使用枚举类管理
3. 分环境部署时，不同环境的执行器应该配置不同的版本标识
4. 当使用默认版本时，系统行为与原版保持一致，确保兼容性
5. 版本信息在执行器启动注册时传递给调度中心，并持久化存储
6. 实现环境隔离后，请确保系统容错机制正常工作，避免单点故障

## 实现原理总结

1. **版本标识传递链路**：
   - 执行器启动时设置版本信息 → XxlJobExecutor
   - XxlJobExecutor 初始化 EmbedServer 时传递版本
   - EmbedServer 启动时传递版本给 ExecutorRegistryThread
   - ExecutorRegistryThread 注册时将版本信息发送给调度中心

2. **调度中心处理流程**：
   - 接收执行器注册请求，存储带版本信息的注册数据
   - 任务触发时，根据任务版本筛选对应版本的执行器
   - 使用过滤后的执行器地址执行任务调度

3. **数据流向**：
   - 执行器 → 调度中心：注册时携带版本
   - 调度中心 → 数据库：存储版本信息
   - 调度中心 → 执行器：触发任务时根据版本选择

实现这种环境隔离机制后，可以在同一个调度中心同时管理多个环境的任务和执行器，大大提高了系统的灵活性和资源利用率，同时避免了跨环境调度带来的问题。 