# controller/ — 控制器层（API 接口）

接收前端 HTTP 请求，调用 Service 层处理业务，返回 JSON 响应。
每个 Controller 对应一个业务模块，不做业务逻辑处理，只做请求分发和参数校验。

| 文件名 | 功能说明 |
|--------|----------|
| AuthController.java | 用户认证接口：登录注册、验证码、密保问题验证 |
| PetController.java | 宠物信息管理接口：宠物 CRUD、搜索筛选、状态变更 |
| CategoryController.java | 宠物分类管理接口：分类的增删改查 |
| AdoptController.java | 领养申请接口：提交申请、审核处理、进度查询 |
| CommentController.java | 宠物评论接口：发表评论与回复、评论列表查询 |
| FavoriteController.java | 宠物收藏接口：收藏/取消收藏、收藏列表查询 |
| NotificationController.java | 通知管理接口：通知列表、已读标记、批量删除 |
| UploadController.java | 文件上传接口：图片文件上传与存储 |
| StatsController.java | 数据统计接口：平台运营数据统计查询 |
| ProfileController.java | 个人中心接口：用户资料查看与信息编辑 |
| ShelterController.java | 收容所管理接口：收容所 CRUD 与信息维护 |
| UserController.java | 用户管理接口：管理员操作用户查询和状态管理 |
| RecordController.java | 领养记录接口：领养记录查询、回访记录管理 |
| CleanupController.java | 清理重置接口：测试/运维用的数据清理操作 |
| AdoptionGuideController.java | 领养须知接口：领养指南的增删改查与前台展示 |
| PetTipController.java | 小常识接口：宠物养护知识的增删改查与前台展示 |
| AdoptionStoryController.java | 领养故事接口：领养故事的发布审核与前台展示 |
