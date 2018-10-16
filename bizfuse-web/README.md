## Bizfuse3.0 通用的 WEB 依赖包

### 应用场景描述

- 需要快速构建一个Web工程, 提供最基本的Rest服务
- 要支持必要的服务监控  
- 方便配置服务度量指标和实时获取度量值
- 支持本地和分布式缓存
- 支持本地和分布式日志管理
- 自动提供动态的API接口文档

### 功能描述
通用的Web依赖包, 结合了很多流行的微服务开源框架  

- 集成SpringMVC, 对外提供最基本的Rest服务
- 集成Swagger, 能够支持为Restful接口生成动态的接口文档, 减少项目接口文档的维护成本
- 集成Spring-Boot-Actuator, 提供对应用的监控和配置查看功能
- 集成Metrics, 一个通用的度量统计框架, 可以方便的对业务代码的各个指标进行监控
- 集成hazelcast, 支持分布式缓存

以下为工程提供的共通

1. 接口返回结构的封装; Convertor基类的实现, 提供Model与DTO转换的通用方法; 通用的Bean定义(比如键值对结构)
2. Request请求参数自定义参数类型封装. 比如 Searchable
3. 异常的统一处理和国际化. Controller层面拦截所有抛出的异常, 国际化异常信息, 以统一的格式输出给客户端
4. 日志管理, 查看和动态修改日志级别
5. Excel导出功能
6. 提供常用的工具类


### 快速使用手册
- 添加Mave工程的依赖到pom.xml

```xml
    // 继承Bizfuse3.0
    <parent>
		<groupId>com.leadingsoft.bizfuse</groupId>
		<artifactId>bizfuse-parent</artifactId>
		<version>3.0-SNAPSHOT</version>
	</parent>
	
	// dependencies 添加依赖包
	<dependencies>
		<!-- bizfuse-web-common dependencies -->
		<dependency>
			<groupId>com.leadingsoft.bizfuse</groupId>
			<artifactId>bizfuse-web-common</artifactId>
		</dependency>
		
		...
	</dependencies>
```

- 配置支持可运行的jar包启动. Application项目启动类添加 @EnableBizfuseWebMVC 使功能生效, 参考代码如下:

```
@EnableBizfuseWebMVC
public class BizfuseWebApplication {

    private static final Logger log = LoggerFactory.getLogger(BizfuseWebApplication.class);

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments
     * @throws UnknownHostException if the local host name could not be resolved
     *         into an address
     */
    public static void main(final String[] args) throws UnknownHostException {
        final SpringApplication app = new SpringApplication(BizfuseWebApplication.class);

        // 输出服务地址和接口信息
        final Environment env = app.run(args).getEnvironment();
        BizfuseWebApplication.log.info("\n----------------------------------------------------------\n\t" +
                "Application '{}' is running! Access URLs:\n\t" +
                "Local: \t\thttp://127.0.0.1:{}\n\t" +
                "External: \thttp://{}:{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                env.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"));

        final String configServerStatus = env.getProperty("configserver.status");
        BizfuseWebApplication.log.info("\n----------------------------------------------------------\n\t" +
                "Config Server: \t{}\n----------------------------------------------------------",
                configServerStatus == null ? "Not found or not setup for this application" : configServerStatus);
    }
}
```

- 配置支持生成容器中可运行的war包. 添加SpringBootServletInitializer实现类, 参考下面的示例:

```java
public class ApplicationWebXml extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return application.sources(BizfuseWebApplication.class);
    }
}
```

- 核心的配置项 (在src/main/resources/config/application.yml中配置)
spring-boot的通用配置参考官方文档  http://docs.spring.io/spring-boot/docs/1.4.0.RELEASE/reference/html/common-application-properties.html

```
bizfuse: 
    web: 
        async: # 异步线程池配置, 默认生效
            corePoolSize: 2 # the number of threads to keep in the pool, even if they are idle 默认值 2
            maxPoolSize: 50 # the maximum number of threads to allow in the pool 默认值 50
            queueCapacity: # the queue size to use for holding tasks before they are executed 默认值 10000
        #cors: # 跨区配置, 默认不生效. 取消注释才能生效.
            #allowed-origins: "*"
            #allowed-methods: GET, PUT, POST, DELETE, OPTIONS
            #allowed-headers: "*"
            #exposed-headers:
            #allow-credentials: true
            #max-age: 1800
        swagger: # Swagger动态接口文档配置
            title: BizfuseWebMVC API # 文档标题
            description: BizfuseWebSample API documentation # 文档描述
            version: 0.0.1 # API版本号
            termsOfServiceUrl: # 服务条款访问地址
            contactName: # 联系人
            contactUrl:  # 联系地址
            contactEmail: # 联系邮箱
            license:
            licenseUrl: 
            pathPatterns: /management/.*,/api/.*  # API 路径匹配表达式, 支持逗号分割
        metrics: # DropWizard Metrics configuration, used by MetricsConfiguration
            jmx.enabled: true 
            spark:
                enabled: false
                host: localhost
                port: 9999
            graphite:
                enabled: false
                host: localhost
                port: 2003
                prefix: microapp
            logs: # Reports Dropwizard metrics in the logs
                enabled: false
                reportFrequency: 60 # in seconds
        logging:
            logstash: # Forward logs to logstash over a socket, used by LoggingConfiguration
                enabled: false
                host: localhost
                port: 5000
                queueSize: 512
            spectator-metrics: # Reports Spectator Circuit Breaker metrics in the logs
                enabled: false
```

### 参考文档
 Spring-Boot-Actuator https://segmentfault.com/a/1190000004318360
 http://www.baeldung.com/spring-boot-actuators  
 
 