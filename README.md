# 小芽记账

小芽记账是一个基于 uni-app 和 Spring Cloud 的个人记账应用第一版，支持用户管理账单、查看收支统计和设置分类预算。

## 功能

- 用户注册、登录和退出登录
- 账单新增、编辑、删除、月份筛选、分类筛选和分页
- 月度收入、支出、结余及分类支出统计
- 分类预算设置、清除和使用情况查看
- Gateway 统一路由与 JWT 身份校验

## 技术栈

- 前端：uni-app、Vue 3
- 后端：JDK 21、Spring Boot、Spring Cloud
- 网关：Spring Cloud Gateway
- 服务注册与发现：Eureka
- 配置中心：Apollo
- 数据存储：MySQL、Redis
- 持久层：MyBatis、Flyway

## 项目结构

- `app-uniapp-vue3/app-uniapp-vue3`：前端项目
- `xiaoya-backend`：后端 Maven 聚合工程
- `xiaoya-backend/xiaoya-eureka-server`：服务注册中心
- `xiaoya-backend/xiaoya-gateway`：API 网关
- `xiaoya-backend/xiaoya-user-service`：用户服务
- `xiaoya-backend/xiaoya-accounting-service`：记账、预算和统计服务
- `docs`：接口文档、数据库设计和项目规划

## 本地运行条件

需要准备 JDK 21、Maven、MySQL、Redis、Apollo 和 HBuilderX。

1. 启动 MySQL、Redis、Apollo 和 Eureka。
2. 在 Apollo 中配置各服务所需的数据库连接、Redis、Eureka 地址及 JWT 密钥。
3. 设置后端所需的环境变量，例如 `DB_PASSWORD` 和 `XIAOYA_JWT_SECRET`。不要将真实密码或密钥提交到仓库。
4. 启动用户服务、记账服务和 Gateway。
5. 使用 HBuilderX 打开 `app-uniapp-vue3/app-uniapp-vue3`，运行到 H5。
6. 本地 H5 默认通过 `http://localhost:8080` 访问 Gateway。

后端接口和数据库设计详见 `docs` 目录。

## 当前状态

这是持续迭代中的第一版项目。当前版本已在本地完成主要功能联调；尚未部署为长期在线服务。
