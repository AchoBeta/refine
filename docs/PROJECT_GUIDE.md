# AchoBeta Refine 项目结构与部署指南

> **注意**: 本文档由AI生成!!(意思是可能有不对的地方)

本文面向开发者，简洁而详尽地说明本仓库的模块结构、主要配置文件位置。

- 技术栈与版本：JDK 8、Spring Boot 2.7.12、Maven、MyBatis、MySQL 8、Redis 6（可选）
- 默认端口：8091（可用环境变量 SERVER_PORT 覆盖）
- 默认 Profile：dev（可通过 -Dspring.profiles.active 或 SPRING_PROFILES_ACTIVE 覆盖）

---

## 1. 模块总览
本仓库为多模块 Maven 工程（父 POM 为 `pom.xml`），模块如下：

- refine-api：对外或对内暴露的接口定义/DTO 等
- refine-domain：领域模型与业务规则
- refine-infrastructure：基础设施层（持久化、第三方集成等）
- refine-trigger：触发/适配层
- refine-types：通用类型/常量/工具类
- refine-app：可运行的 Spring Boot 应用（打包与部署入口）

父 POM 统一管理依赖与插件，并定义了 `dev/test/prod` 三个 Maven profile。

---

## 2. 目录与关键文件
以下为与开发/部署直接相关的关键目录与文件：

- 根目录
  - `pom.xml`：父 POM，声明模块与依赖管理
  - `README.md`：项目说明
  - `docs/`：文档与 DevOps 相关文件
    - `docs/dev-ops/docker-compose-environment.yml`：本地/测试依赖环境（MySQL、Redis、phpMyAdmin、Redis-Commander）
    - `docs/dev-ops/docker-compose-environment-aliyun.yml`：阿里云镜像源版本
    - `docs/dev-ops/docker-compose-app.yml`：应用容器
    - `docs/dev-ops/app/start.sh`、`docs/dev-ops/app/stop.sh`：容器启停脚本（Linux/macOS 环境）

- 应用模块 `refine-app/`
  - `src/main/resources/application.yml`：主配置（默认启用 `dev`）
  - `src/main/resources/application-dev.yml`：开发环境配置

---

## 3. Spring 配置说明

- `application.yml`
  - 指定应用名与默认激活 Profile：
    - `spring.config.name=refine-app`
    - `spring.profiles.active=dev`

- `application-*.yml`（以 `dev` 为例）
  - `server.port`：默认 8091
  - `app.config.api-version`、`app.config.cross-origin`：应用级配置（可通过环境变量覆盖，见第 4 节）
  - 线程池：`thread.pool.executor.config.*`
  - 数据源：`spring.datasource.*`（默认指向 `127.0.0.1:3306` 数据库 `achobeta-refine`，用户名/密码 `root/123456`）
  - Redis：`redis.sdk.enabled`（默认 false, 启用后使用 `redis.sdk.config.*`）
  - MyBatis：`mybatis.mapper-locations`、`mybatis.config-location`
  - 日志：`logging.level.root`、`logging.config`（logback 配置位于 `classpath:logback-spring.xml`）

- 日志输出（`logback-spring.xml`）
  - 默认目录：`./data/log`（相对应用工作目录）
  - 文件：`log_console.log`、`log_info.log`、`log_error.log`（按天滚动）

