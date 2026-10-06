# entity/ — 实体类（数据模型）

每个实体类对应一张数据库表，使用 MyBatis-Plus 注解完成字段映射。
各实体配合对应的 Mapper 接口，实现零 XML 配置的 ORM 操作。

| 文件名 | 作用 |
|--------|------|
| User.java | 用户实体，对应用户表，存储账号密码、角色、联系方式等信息 |
| PetInfo.java | 宠物信息实体，对应宠物表，描述宠物名称、品种、年龄、状态等 |
| AdoptApplication.java | 领养申请实体，记录用户提交的领养请求及审核状态 |
| AdoptRecord.java | 领养记录实体，记录已完成的领养事务及领养人信息 |
| FollowupRecord.java | 回访记录实体，记录领养后的回访跟踪内容与结果 |
| Notification.java | 通知实体，存储系统推送给用户的通知标题、内容与状态 |
| PetComment.java | 宠物评论实体，存储用户对宠物的评论及评论间的回复关系 |
| PetImage.java | 宠物图片实体，记录宠物关联的多张图片 URL 路径 |
| PetFavorite.java | 宠物收藏实体，记录用户与宠物的收藏关联关系 |
| PetCategory.java | 宠物分类实体，定义宠物的品种、类别名称与描述 |
| ShelterInfo.java | 收容所信息实体，记录收容所的名称、地址、联系方式等基本信息 |
| UserSecurityQuestion.java | 用户密保实体，存储用户设置的密保问题与加密后的答案 |
| AdoptionGuide.java | 领养须知实体，存储领养指南条款及分类 |
| PetTip.java | 小常识实体，存储宠物养护知识的标题、内容和分类 |
| AdoptionStory.java | 领养故事实体，存储领养人分享的故事、图片和感悟 |
