# WordLearn 多语种词汇学习系统

一个前后端分离的词汇学习平台，支持 Web 端和微信小程序双端使用。包含创建的词书、完成每日学习任务、错题本记录等功能，还提供了ECharts数据可视化，方便用户掌握自己的学习进度。新增社区交流和扩展学习资源中心。


# 技术栈

后端：Spring Boot 3.1.5 + Java 17 + MyBatis-Plus + MySQL + JWT认证

Web前端：Vue 3 + Vite + Element Plus + ECharts + Vue Router + Axios

小程序端：微信原生小程序


# 功能说明

用户认证：注册、登录，基于JWT的身份鉴权

词书管理：创建、编辑、删除词书，管理词书下的单词条目

每日学习：系统推送每日学习任务，记录学习进度

错题本：自动收录答错的词汇，支持针对性回顾

数据统计：基于ECharts的学习数据可视化，展示学习趋势

排行榜：用户学习排名，激励学习

学习社区：发帖、评论、点赞，用户间互动交流

拓展学习：管理员可发布学习资源供用户浏览

个人中心：修改用户名、密码、头像


# 项目结构

```
word/
├── backend/                   后端服务（Spring Boot）
│   └── src/main/java/com/example/word/
│       ├── controller/        控制器
│       ├── service/           业务逻辑
│       ├── mapper/            数据访问（MyBatis-Plus）
│       ├── entity/            实体类
│       ├── config/            配置类
│       ├── common/            公共组件
│       └── utils/             工具类
│
├── frontend/                  Web 前端（Vue3）
│   └── src/
│       ├── views/             页面组件
│       ├── api/               接口封装
│       ├── router/            路由
│       ├── components/        公共组件
│       └── utils/             工具函数
│
├── Wechat/                    微信小程序
│   └── pages/                 小程序页面
│
└── wordtreedb.sql             数据库初始化脚本
```


# 部署步骤

1. 创建MySQL 数据库wordtreedb，导入根目录下的wordtreedb.sql脚本。

2. 修改backend/src/main/resources/application.yml 中的数据库连接信息，然后在backend目录下执行mvn spring-boot:run启动后端，默认端口 8082。

3. 在frontend目录下执行npm install安装依赖，然后执行npm run dev启动前端开发服务器，默认端口5173。

4. 使用小程序端，用微信开发者工具打开Wechat目录，修改工具函数中的后端地址后编译。

