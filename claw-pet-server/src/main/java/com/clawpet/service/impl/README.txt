# impl/ — 业务逻辑层（实现类）

service/ 接口的具体实现。核心业务代码在此目录中编写，
如领养申请的审核流程、评论回复的树形组装、统计数据聚合等。

| 文件名 | 作用 |
|--------|------|
| AuthServiceImpl.java | 认证业务实现：登录注册逻辑、密码加密、Token 签发 |
| PetServiceImpl.java | 宠物业务实现：宠物信息管理、搜索与状态变更 |
| CategoryServiceImpl.java | 分类业务实现：宠物分类的增删改查操作 |
| AdoptServiceImpl.java | 领养业务实现：领养申请提交、审核流转处理 |
| CommentServiceImpl.java | 评论业务实现：评论与回复的发表和列表查询 |
| FavoriteServiceImpl.java | 收藏业务实现：收藏/取消收藏逻辑及收藏列表查询 |
| NotificationServiceImpl.java | 通知业务实现：通知消息推送、查询与已读标记 |
| RecordServiceImpl.java | 记录业务实现：领养记录与回访记录的管理操作 |
| ShelterServiceImpl.java | 收容所业务实现：收容所信息的维护管理 |
| StatsServiceImpl.java | 统计业务实现：平台运营数据的统计计算 |
| AdoptionGuideServiceImpl.java | 领养须知业务实现：领养指南的增删改查与排序 |
| PetTipServiceImpl.java | 小常识业务实现：宠物养护知识的分类管理 |
| AdoptionStoryServiceImpl.java | 领养故事业务实现：领养故事的发布审核 |
