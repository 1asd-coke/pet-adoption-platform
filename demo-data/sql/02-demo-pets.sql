-- ========================================
-- Claw Pet 宠物领养系统 - 演示数据
-- ========================================

-- 宠物分类
INSERT INTO pet_category (id, name) VALUES
(1, '猫咪'),
(2, '狗狗'),
(3, '兔子'),
(4, '仓鼠'),
(5, '其他');

-- 宠物信息 (15 只, 全部待领养)
INSERT INTO pet_info (id, name, category_id, breed, age, gender, weight, health_status, vaccine_status, description, status) VALUES
(1, '奶糖', 1, '布偶猫', '2岁', 'female', '4.8kg', 'healthy', 'vaccinated', '蓝眼睛仙女猫，性格温柔粘人。已绝育疫苗。', 'available'),
(2, '年糕', 1, '橘猫', '8个月', 'male', '4.2kg', 'healthy', 'vaccinating', '活力小橘猫，会用猫砂不挑食。正在打疫苗。', 'available'),
(3, '雪宝', 1, '英短银渐层', '1岁', 'female', '3.6kg', 'healthy', 'vaccinated', '银白色小公主，害羞粘人。已绝育疫苗。', 'available'),
(4, '墨宝', 1, '黑猫', '2岁', 'male', '4.5kg', 'healthy', 'vaccinated', '全黑绿眼中华田园猫，神秘帅气。', 'available'),
(5, '铁柱', 2, '中华田园犬', '2岁', 'male', '18kg', 'healthy', 'vaccinated', '黄狗铁柱，看家好手聪明听话。已绝育疫苗。', 'available'),
(6, '团团', 2, '柴犬', '1岁', 'female', '9kg', 'sick', 'vaccinated', '微笑柴犬妹妹，治疗皮肤病中快好了。', 'available'),
(7, '咖啡', 2, '拉布拉多', '2岁', 'male', '26kg', 'healthy', 'vaccinated', '精力充沛爱玩球，已完成基本训练。', 'available'),
(8, '豆花', 2, '边牧', '1岁半', 'female', '15kg', 'healthy', 'vaccinated', '黑白边牧妹妹，会接飞盘和握手。', 'available'),
(9, '跳跳', 3, '荷兰垂耳兔', '1岁', 'male', '1.5kg', 'healthy', 'vaccinated', '垂耳兔小男生，毛茸茸像棉花糖。', 'available'),
(10, '布丁', 3, '荷兰猪', '8个月', 'female', '0.8kg', 'healthy', 'unvaccinated', '胖胖荷兰猪，爱吃提摩西草和水果。', 'available'),
(11, '绒绒', 4, '金丝熊仓鼠', '5个月', 'male', '0.15kg', 'healthy', 'unvaccinated', '奶茶色金丝熊，腮帮子塞满食物超萌。', 'available'),
(12, '彩虹', 5, '虎皮鹦鹉', '2岁', 'female', '0.03kg', 'healthy', 'unvaccinated', '会说"你好"的聪明小鹦鹉，已上手。', 'available'),
(13, '咕咕', 5, '玄凤鹦鹉', '1岁', 'male', '0.08kg', 'healthy', 'unvaccinated', '黄化玄凤，头顶小羽冠超可爱。', 'available'),
(14, '闪电', 5, '蜜袋鼯', '8个月', 'male', '0.12kg', 'healthy', 'unvaccinated', '会滑翔的小蜜袋鼯，手掌大小超可爱。', 'available'),
(15, '憨憨', 2, '法斗', '3岁', 'male', '13kg', 'healthy', 'vaccinated', '丑萌法斗，不爱叫不拆家。已绝育疫苗。', 'available');

-- 宠物图片（每只一张封面）
-- ⚠️ 图片文件不在 claw-pet-server/upload/pet/ 里，而是随仓库放在 demo-data/images/pet/
--    首次部署：把 demo-data/images/pet/*.jpg 全部拷到 claw-pet-server/upload/pet/ 即可
--    （详见 demo-data/README.md；用 start.bat / scripts/start.sh 启动会自动拷）
--    路径必须带前导斜杠，前端是当绝对路径用的
INSERT INTO pet_image (pet_id, url, is_cover, sort) VALUES
(1,  '/profile/pet/demo-01.jpg', 1, 0),
(2,  '/profile/pet/demo-02.jpg', 1, 0),
(3,  '/profile/pet/demo-03.jpg', 1, 0),
(4,  '/profile/pet/demo-04.jpg', 1, 0),
(5,  '/profile/pet/demo-05.jpg', 1, 0),
(6,  '/profile/pet/demo-06.jpg', 1, 0),
(7,  '/profile/pet/demo-07.jpg', 1, 0),
(8,  '/profile/pet/demo-08.jpg', 1, 0),
(9,  '/profile/pet/demo-09.jpg', 1, 0),
(10, '/profile/pet/demo-10.jpg', 1, 0),
(11, '/profile/pet/demo-11.jpg', 1, 0),
(12, '/profile/pet/demo-12.jpg', 1, 0),
(13, '/profile/pet/demo-13.jpg', 1, 0),
(14, '/profile/pet/demo-14.jpg', 1, 0),
(15, '/profile/pet/demo-15.jpg', 1, 0);

-- 收容所图片（首页 hero 图），对应 demo-data/images/pet/shelter.jpg
-- 这里用 UPDATE 而不是 INSERT，方便对已建库的存量数据做修正
UPDATE shelter_info SET image = '/profile/pet/shelter.jpg' WHERE id = 1;
