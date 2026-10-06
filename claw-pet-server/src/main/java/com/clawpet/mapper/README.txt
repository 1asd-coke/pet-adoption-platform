# mapper/ — MyBatis Mapper 接口（数据访问层）

每个 Mapper 对应一个 Entity 和一张数据库表，继承 MyBatis-Plus 的 BaseMapper 接口，
无需手写 SQL 即可完成基本的增删改查操作。复杂查询可通过 @Select 注解或 XML 文件实现。

| 文件名 | 作用 |
|--------|------|
| UserMapper.java | 用户表数据访问接口 |
| PetInfoMapper.java | 宠物信息表数据访问接口 |
| AdoptApplicationMapper.java | 领养申请表数据访问接口 |
| AdoptRecordMapper.java | 领养记录表数据访问接口 |
| FollowupRecordMapper.java | 回访记录表数据访问接口 |
| NotificationMapper.java | 通知表数据访问接口 |
| PetCommentMapper.java | 宠物评论表数据访问接口 |
| PetImageMapper.java | 宠物图片表数据访问接口 |
| PetFavoriteMapper.java | 宠物收藏表数据访问接口 |
| PetCategoryMapper.java | 宠物分类表数据访问接口 |
| ShelterInfoMapper.java | 收容所信息表数据访问接口 |
| UserSecurityQuestionMapper.java | 用户密保表数据访问接口 |
| AdoptionGuideMapper.java | 领养须知表数据访问接口 |
| PetTipMapper.java | 小常识表数据访问接口 |
| AdoptionStoryMapper.java | 领养故事表数据访问接口 |
