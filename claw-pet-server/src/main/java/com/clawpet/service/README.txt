# service/ — 业务逻辑层（接口）

定义业务逻辑接口，一个 Service 对应一个业务模块。
接口声明业务方法签名，具体实现在 impl/ 子目录中，命名如 AuthServiceImpl 实现 AuthService。

| 文件名 | 作用 |
|--------|------|
| AuthService.java | 认证业务接口：登录注册、密码加密验证、密保问题校验 |
| PetService.java | 宠物业务接口：宠物信息管理、搜索筛选、状态流转 |
| CategoryService.java | 分类业务接口：宠物分类的增删改查 |
| AdoptService.java | 领养业务接口：领养申请提交、审核流程处理 |
| CommentService.java | 评论业务接口：宠物评论与回复的发表和查询 |
| FavoriteService.java | 收藏业务接口：收藏/取消收藏操作及收藏列表查询 |
| NotificationService.java | 通知业务接口：通知消息的发送、查询与状态管理 |
| RecordService.java | 记录业务接口：领养记录与回访记录的查询和管理 |
| ShelterService.java | 收容所业务接口：收容所信息的维护管理 |
| StatsService.java | 统计业务接口：平台运营数据的聚合计算与查询 |
| AdoptionGuideService.java | 领养须知业务接口：领养指南的发布与管理 |
| PetTipService.java | 小常识业务接口：宠物养护知识的内容管理 |
| AdoptionStoryService.java | 领养故事业务接口：领养故事的发布与展示 |
