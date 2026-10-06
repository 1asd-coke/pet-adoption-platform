-- ========================================
-- Claw Pet -- 数据库建表脚本
-- 生成时间: 2026-07-19
-- ========================================

CREATE TABLE `pet_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(200) DEFAULT '' COMMENT '图标',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` varchar(10) DEFAULT 'active' COMMENT '状态: active/disabled',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宠物分类表';

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT '' COMMENT '昵称',
  `email` varchar(100) DEFAULT '' COMMENT '邮箱',
  `phone` varchar(20) DEFAULT '' COMMENT '手机号',
  `real_name` varchar(50) DEFAULT '' COMMENT '真实姓名',
  `id_card` varchar(20) DEFAULT '' COMMENT '身份证号',
  `address` varchar(200) DEFAULT '' COMMENT '地址',
  `avatar` varchar(500) DEFAULT '' COMMENT '头像URL',
  `auth_status` varchar(10) DEFAULT 'unauth' COMMENT '认证状态: unauth/pending/verified',
  `role` varchar(20) NOT NULL DEFAULT 'user' COMMENT '角色: admin/user',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE `pet_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '宠物ID',
  `name` varchar(100) NOT NULL COMMENT '宠物名称',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `breed` varchar(100) DEFAULT '' COMMENT '品种',
  `age` varchar(20) DEFAULT '' COMMENT '年龄',
  `gender` varchar(10) DEFAULT 'unknown' COMMENT '性别: male/female/unknown',
  `weight` varchar(20) DEFAULT '' COMMENT '体重',
  `health_status` varchar(20) DEFAULT 'healthy' COMMENT '健康: healthy/sick/recovering',
  `vaccine_status` varchar(20) DEFAULT 'unvaccinated' COMMENT '疫苗: unvaccinated/vaccinated/vaccinating',
  `description` text COMMENT '描述',
  `status` varchar(20) DEFAULT 'available' COMMENT '状态: available/adopted/offline',
  `publish_user_id` bigint DEFAULT NULL COMMENT '发布人ID',
  `view_count` int DEFAULT '0' COMMENT '浏览次数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宠物信息表';

CREATE TABLE `pet_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pet_id` bigint NOT NULL COMMENT '宠物ID',
  `url` varchar(500) NOT NULL COMMENT '图片URL',
  `is_cover` tinyint(1) DEFAULT '0' COMMENT '是否封面 0否 1是',
  `sort` int DEFAULT '0' COMMENT '排序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宠物图片表';

CREATE TABLE `shelter_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(100) DEFAULT '' COMMENT '收容所名称',
  `address` varchar(300) DEFAULT '' COMMENT '地址',
  `phone` varchar(50) DEFAULT '' COMMENT '联系电话',
  `email` varchar(100) DEFAULT '' COMMENT '邮箱',
  `work_hours` varchar(200) DEFAULT '' COMMENT '营业时间',
  `wechat` varchar(100) DEFAULT '' COMMENT '微信号',
  `description` text COMMENT '简介描述',
  `image` varchar(500) DEFAULT '' COMMENT '展示图片',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收容所信息表';

CREATE TABLE `pet_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `pet_id` bigint DEFAULT NULL COMMENT '宠物ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `content` varchar(1000) NOT NULL COMMENT '评论内容',
  `parent_id` bigint DEFAULT '0' COMMENT '父评论ID',
  `reply_to_user_id` bigint DEFAULT NULL COMMENT '回复目标用户ID',
  `status` varchar(10) DEFAULT 'active' COMMENT '状态: active/hidden',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pet` (`pet_id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';

CREATE TABLE `pet_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `pet_id` bigint DEFAULT NULL COMMENT '宠物ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pet` (`user_id`,`pet_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏表';

CREATE TABLE `adopt_application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `pet_id` bigint DEFAULT NULL COMMENT '宠物ID',
  `user_id` bigint DEFAULT NULL COMMENT '申请人ID',
  `reason` text COMMENT '领养理由',
  `housing_condition` varchar(200) DEFAULT '' COMMENT '住房条件',
  `experience` varchar(200) DEFAULT '' COMMENT '养宠经验',
  `contact_phone` varchar(20) DEFAULT '' COMMENT '联系电话',
  `contact_address` varchar(200) DEFAULT '' COMMENT '联系地址',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
  `reject_reason` varchar(500) DEFAULT '' COMMENT '拒绝原因',
  `review_user_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pet` (`pet_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领养申请表';

CREATE TABLE `adopt_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `application_id` bigint DEFAULT NULL COMMENT '申请ID',
  `pet_id` bigint DEFAULT NULL COMMENT '宠物ID',
  `user_id` bigint DEFAULT NULL COMMENT '领养人ID',
  `adopt_time` datetime DEFAULT NULL COMMENT '领养时间',
  `followup_status` varchar(20) DEFAULT 'pending' COMMENT '回访: pending/done',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_application` (`application_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领养记录表';

CREATE TABLE `followup_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回访ID',
  `record_id` bigint DEFAULT NULL COMMENT '领养记录ID',
  `content` text COMMENT '回访内容',
  `images` varchar(2000) DEFAULT '' COMMENT '回访图片',
  `followup_time` datetime DEFAULT NULL COMMENT '回访时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='回访记录表';

CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收通知的用户ID',
  `type` varchar(20) NOT NULL COMMENT '通知类型',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知正文',
  `related_id` bigint DEFAULT NULL COMMENT '关联ID',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '0未读 1已读',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`is_read`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知表';

CREATE TABLE `user_security_question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `question` varchar(255) NOT NULL COMMENT '密保问题',
  `answer` varchar(255) NOT NULL COMMENT '密保答案（BCrypt 加密）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='密保问题表';


-- ========================================
-- 二：新增功能表（领养须知 / 小常识 / 领养故事）
-- ========================================

CREATE TABLE IF NOT EXISTS adoption_guide (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL DEFAULT '领养须知',
  content MEDIUMTEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pet_tip (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  publish_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  sort INT DEFAULT 0,
  deleted INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_publish_date (publish_date),
  INDEX idx_deleted (deleted)
);

CREATE TABLE IF NOT EXISTS adoption_story (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  record_id BIGINT NOT NULL,
  pet_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  story MEDIUMTEXT,
  showcase TINYINT(1) DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_record (record_id),
  INDEX idx_user (user_id),
  INDEX idx_showcase (showcase)
);

-- ========================================
-- 三：默认数据
-- ========================================

-- 默认管理员（密码 admin123 / BCrypt 加密）
INSERT IGNORE INTO users (id, username, password, role) VALUES
(1, 'admin', '$2b$10$IkazYtHNcjZnPVEESwya5e5fULG8qwU6gZVVn8Du5TkLcrkq6Ngda', 'admin'),
(2, 'user', '$2b$10$IkazYtHNcjZnPVEESwya5e5fULG8qwU6gZVVn8Du5TkLcrkq6Ngda', 'user');

-- 上海领养中心信息
-- image 指向 demo-data/images/pet/shelter.jpg（随仓库分发，启动脚本会自动拷到 claw-pet-server/upload/pet/）
INSERT INTO shelter_info (id, name, address, phone, email, work_hours, wechat, description, image) VALUES (
  1,
  'Claw Pet 上海领养中心',
  '上海市静安区南京西路 1601 号',
  '021-58886666',
  'shanghai@clawpet.com',
  '周一至周日 10:00-20:00',
  'ClawPet_SH',
  '欢迎来到 Claw Pet 上海领养中心！我们位于静安核心商圈，是一家致力于为流浪动物寻找温暖家庭的非营利机构。中心占地 800 平方米，可同时容纳 50 只猫狗，所有动物均经过专业兽医团队的健康评估和行为训练。期待您来与我们一起，为这些小生命找到属于它们的家。',
  '/profile/pet/shelter.jpg'
) ON DUPLICATE KEY UPDATE
  name = VALUES(name), address = VALUES(address), phone = VALUES(phone),
  email = VALUES(email), work_hours = VALUES(work_hours), wechat = VALUES(wechat),
  description = VALUES(description), image = VALUES(image);

-- ========================================
-- 五：领养须知初始内容
-- ========================================

INSERT INTO adoption_guide (id, title, content) VALUES (
  1,
  '领养须知',
  '1、领养人需年满 22 周岁，具有完全民事行为能力，有稳定的工作和收入来源，能为宠物提供长期稳定的生活条件。\n2、本平台仅支持同城领养，方便后续回访跟进；异地申请暂不受理。\n3、申请领养时请认真填写联系方式、家庭住址，并保持手机畅通；工作人员会在 3 个工作日内通过电话或微信与您联系。\n4、领养不收取任何费用。首次领养建议到 Claw Pet 上海领养中心实地探访，与待领养宠物见面后再做决定。\n5、所有领养宠物均已完成基础疫苗接种和绝育手术，并经过 7 天健康观察期；幼龄宠物在适龄后会由收容所统一安排绝育。\n6、领养人需保证居住环境安全：阳台需加装防护网、窗户需安装防护栏，避免宠物发生坠楼、跑失等意外。\n7、领养人需加入 Claw Pet 上海领养互助群，定期在群内分享宠物近况（照片/视频）；长期不分享且联系不上者，工作人员将主动上门回访。\n8、领养后请定期带宠物到正规宠物医院进行体检、疫苗加强和驱虫；宠物生病时应及时就医，不得自行用药。\n9、领养后如因工作变动、住所变更、家庭变故等无法继续饲养，请第一时间联系 Claw Pet 工作人员，我们会协助安排新领养家庭；任何情况下请勿将宠物遗弃街头。\n10、如领养的宠物不慎走失，请在 24 小时内联系 Claw Pet 工作人员；走失宠物在走失后 3 天内找回的概率最高，及时联系能大幅提升找回成功率。\n11、本平台保留对违反领养协议者的追责权利，包括但不限于：收回宠物、取消后续领养资格、必要时追究法律责任。\n12、本须知最终解释权归 Claw Pet 宠物领养平台所有。如有疑问，请通过站内消息联系客服。\n13、来访路线：地铁 2 号线 / 7 号线 静安寺站 6 号口出，沿南京西路步行约 600 米即到；自驾可停对面梅龙镇广场 B2 停车场（首小时免费）。\n14、工作时间：周一至周日 10:00-20:00（含节假日）；如需周末上门看猫狗请提前 1 天微信预约（微信号 ClawPet_SH）。'
) ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  content = VALUES(content);

-- 第二条：法律法规
INSERT INTO adoption_guide (id, title, content) VALUES (
  2,
  '相关法律依据',
  '一、宠物饲养与管理\n根据《中华人民共和国民法典》第一千二百四十五条，饲养的动物造成他人损害的，动物饲养人或者管理人应当承担侵权责任，除非能证明损害是因被侵权人故意或者重大过失造成的。这要求宠物收容中心在管理宠物时，需采取必要措施防止宠物伤人。\n\n依据《中华人民共和国民法典》第一千二百四十六条，若违反管理规定未对动物采取安全措施造成他人损害，同样应承担侵权责任。宠物收容中心需确保宠物在收容期间得到妥善管理，避免对他人造成损害。\n\n《中华人民共和国民法典》第一千二百四十七条规定，禁止饲养的烈性犬等危险动物造成他人损害的，动物饲养人或者管理人应承担严格侵权责任。宠物收容中心不得收容禁止饲养的烈性犬等危险动物。\n\n二、动物防疫\n宠物收容中心需遵守《中华人民共和国动物防疫法》的相关规定，定期为收容的宠物接种疫苗，进行疫病预防和控制，确保宠物健康，防止疫病传播。\n\n三、市容环境卫生管理\n根据《城市市容和环境卫生管理条例》，宠物收容中心在公共场所活动时，需及时清理宠物的排泄物，维护公共环境卫生。同时，需遵守地方关于宠物进入公共场所的限制规定，确保公共秩序和卫生。'
) ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  content = VALUES(content);

-- ========================================
-- 六：小常识初始内容
-- ========================================

INSERT INTO pet_tip (title, content, sort) VALUES
('领养宠物的准备', '在领养之前，请确保您已经准备好以下物品：\n1. 食盆、水盆\n2. 合适的宠物粮食\n3. 牵引绳、项圈（狗狗）\n4. 猫砂盆、猫砂（猫咪）\n5. 舒适的窝或垫子\n6. 适合的玩具\n7. 身份证件', 1),
('如何与新领养的宠物建立信任', '新领养的宠物需要时间适应新环境，建议：\n1. 给它一个安静的小空间，让它能躲藏休息\n2. 不要急着强行互动，让它主动接近你\n3. 用零食建立正面关联\n4. 保持规律喂食和作息\n5. 多用温和的语气和缓慢的动作\n6. 第一次 1-2 周内尽量不带它外出', 2),
('宠物定期体检的重要性', '即使是领养的健康宠物，也建议：\n1. 每年至少体检一次\n2. 按时接种疫苗\n3. 定期驱虫（体内 + 体外）\n4. 绝育手术建议在 6-12 月龄进行\n5. 出现异常及时就医，不要自行用药', 3);
