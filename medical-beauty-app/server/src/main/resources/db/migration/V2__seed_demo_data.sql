-- ============================================================================
-- 演示数据：基于真实 PPT《彭蕾抗衰整体规划方案》构造
-- 用于一期 MVP 验证 + 顾问培训
-- ============================================================================

-- 1. 租户 (北京思达芮医疗美容诊所)
INSERT INTO `tenant` (`id`, `name`, `legal_entity`, `brand_primary_color`, `status`)
VALUES (1, 'STARRY · 思达芮', '北京思达芮医疗美容诊所有限公司', '#0A0A0A', 2);

-- 2. 门店 (旗舰店)
INSERT INTO `store` (`id`, `tenant_id`, `name`, `address`, `business_hours`, `status`)
VALUES (1, 1, '思达芮旗舰店', '北京市朝阳区',
        JSON_OBJECT('mon', JSON_ARRAY('10:00','21:00'),
                    'tue', JSON_ARRAY('10:00','21:00'),
                    'wed', JSON_ARRAY('10:00','21:00'),
                    'thu', JSON_ARRAY('10:00','21:00'),
                    'fri', JSON_ARRAY('10:00','21:00'),
                    'sat', JSON_ARRAY('10:00','21:00'),
                    'sun', JSON_ARRAY('10:00','21:00')),
        1);

-- 3. 顾问
INSERT INTO `employee` (`id`, `tenant_id`, `store_id`, `name`, `job_title`, `role_code`, `status`)
VALUES (1, 1, 1, '沈妍希', '咨询顾问', 'CONSULTANT', 1);

-- 4. 项目 (来自 PPT)
INSERT INTO `project` (`id`, `tenant_id`, `name`, `category`, `description`, `science_text`, `default_cycle_rule`, `unit_price`, `status`) VALUES
(1, 1, '黑金超光子', '光电',
 '改善皮肤黑色素沉淀、肤色暗黄、毛孔粗大、细纹',
 '采用强脉冲光 IPL 技术，靶向作用于色素和血管病变，刺激胶原再生。3 次为一个疗程。',
 JSON_OBJECT('phase1', JSON_OBJECT('count', 3, 'interval_days', 28)),
 3333.33, 1),

(2, 1, '双生水光', '注射',
 '皮肤营养补充：胶原蛋白类水光 + 童颜水光',
 '复合配方深层补水、补充胶原蛋白，改善皮肤暗沉与干燥，提升光泽与弹性。',
 JSON_OBJECT('phase1', JSON_OBJECT('count', 3, 'interval_days', 30),
             'phase2', JSON_OBJECT('interval_days', 75)),
 4360.00, 1),

(3, 1, '芮艾缇少女枪', '光电',
 '皮肤松垂改善 · 紧致提升',
 '聚焦超声（HIFU）作用于 SMAS 筋膜层，从深层提拉、紧致皮肤，改善松垂。',
 JSON_OBJECT('phase1', JSON_OBJECT('count', 1, 'interval_days', 365)),
 5980.00, 1),

(4, 1, '伊妍仕少女针', '注射',
 'PCL 类长效胶原刺激剂 · 抗衰填充',
 '主要成分聚己内酯（PCL），刺激自体胶原蛋白持续生成，效果维持 2 年以上。',
 JSON_OBJECT('phase1', JSON_OBJECT('count', 1, 'interval_days', 730)),
 15800.00, 1),

(5, 1, '艾维岚童颜针', '注射',
 'PLLA 类胶原刺激剂',
 '聚左旋乳酸（PLLA），刺激胶原新生，整体提拉与轮廓改善。',
 JSON_OBJECT('phase1', JSON_OBJECT('count', 3, 'interval_days', 45)),
 8800.00, 1),

(6, 1, '衡力（包年）', '注射',
 '川字纹放松 · 抗皱',
 'A 型肉毒素，松弛过度活跃的表情肌，改善动态纹路。包年套餐。',
 JSON_OBJECT('phase1', JSON_OBJECT('count', 3, 'interval_days', 120)),
 3980.00, 1);

-- 5. 客户 (彭蕾，55 岁 — PPT 原型客户)
INSERT INTO `customer` (`id`, `tenant_id`, `store_id`, `openid`, `nickname`,
                        `gender`, `birthday`, `level_code`, `consultant_id`,
                        `first_visit_at`, `last_visit_at`, `status`)
VALUES (1, 1, 1, 'demo_openid_penglei', '彭蕾',
        1, '1970-05-04', 'GOLD', 1,
        '2025-05-04 10:00:00', '2025-05-04 10:00:00', 1);

-- 6. 规划方案 (PPT 同款)
INSERT INTO `plan` (`id`, `tenant_id`, `store_id`, `customer_id`, `title`, `subtitle`,
                    `consultant_id`, `valid_from`, `valid_to`,
                    `total_price`, `discount_price`,
                    `analysis_text`, `status`, `pushed_at`, `created_by`)
VALUES (1, 1, 1, 1,
        '彭蕾 · 抗衰整体规划方案',
        '面部抗衰 · 皮肤管理 · 长期跟踪',
        1, '2025-05-04', '2026-05-04',
        81160.00, 75180.00,
        '## 面部皮肤状况分析\n\n**优势**：皮肤紧致度相对较好、五官精致，角质层健康程度良好。\n\n**劣势**：毛孔粗大、肤色暗黄、色素沉淀形成皮肤底层色斑严重、皱纹加重、皮肤松弛、衰老快。\n\n## 面部衰老状况分析\n\n**优势**：五官精致。\n\n**劣势**：三庭比例上庭偏短，眼周衰老造成双眼皮松垂、眼周细纹增多、眼眶骨流失、眼睛微凸；口周衰老、口角囊袋明显、下巴下颌骨衔接不流畅；外轮廓不清晰、中面部松垂、苹果肌下移、法令纹加深、太阳穴凹陷。',
        2, '2025-05-04 14:30:00', 1);

-- 7. 规划方案章节
INSERT INTO `plan_section` (`tenant_id`, `plan_id`, `sort`, `type`, `title`, `content`) VALUES
(1, 1, 1, 'analysis',     '面部状况分析及抗衰规划', NULL),
(1, 1, 2, 'region_plan',  '第一步：T 区轮廓固定',
 '**1、鼻子、眉骨、印堂**\n- 鼻子：收紧鼻背及周边皮肤组织，预防鼻背纹产生、内眼周皮肤组织松垂、立体支撑年轻化\n- 眉骨：有效支撑眼部、预防上面部皮肤组织松垂、提升上眼皮，眼眶骨有效支撑\n- 印堂：鼻部和眼皮皮肤收紧、有效衔接\n\n**2、下巴下颌骨**\n- 有效支撑预防嘴角肉，与下巴做好衔接，减缓双下巴形成，下颌线清晰'),
(1, 1, 3, 'region_plan',  '第二步：下庭抗衰',
 '**1、口周年轻化**：解决法令纹、口角囊袋、收紧口周皮肤，下庭年轻化\n\n**2、唇**：弱化人中长显老、改善唇纹，让唇部回到年轻态'),
(1, 1, 4, 'project_list', '皮肤项目建议',
 '1. **皮肤营养补充**：胶原蛋白类水光、童颜水光\n2. **皮肤黑色素改善**：黑金超光子\n3. **皮肤松垂改善**：芮艾缇少女枪'),
(1, 1, 5, 'material',     '抗衰材料推荐', '伊妍仕少女针 · 艾维岚童颜针 · 人源三型胶原蛋白 · 微笑欣颜'),
(1, 1, 6, 'package',      '套餐活动', NULL);

-- 8. 规划方案项目 (PPT 报价表)
INSERT INTO `plan_item` (`tenant_id`, `plan_id`, `section_id`, `project_id`, `project_name`,
                         `planned_count`, `done_count`, `cycle_rule`,
                         `unit_price`, `activity_price`, `total_price`,
                         `next_due_at`, `status`) VALUES
(1, 1, 6, 1, '黑金超光子', 3, 0,
 JSON_OBJECT('phase1', JSON_OBJECT('count', 3, 'interval_days', 28)),
 3333.33, 3333.33, 10000.00,
 '2025-06-04 10:00:00', 1),

(1, 1, 6, 2, '双生水光', 5, 0,
 JSON_OBJECT('phase1', JSON_OBJECT('count', 3, 'interval_days', 30),
             'phase2', JSON_OBJECT('interval_days', 75)),
 4360.00, 4360.00, 21800.00,
 '2025-06-04 10:00:00', 1),

(1, 1, 6, 3, '芮艾缇少女枪', 1, 0,
 JSON_OBJECT('phase1', JSON_OBJECT('count', 1, 'interval_days', 365)),
 5980.00, 5980.00, 5980.00,
 '2025-06-04 10:00:00', 1),

(1, 1, 6, 4, '伊妍仕少女针 1 代 ×3 支', 3, 0,
 JSON_OBJECT('phase1', JSON_OBJECT('count', 1, 'interval_days', 730)),
 19800.00, 15800.00, 47400.00,
 '2025-06-04 10:00:00', 1),

(1, 1, 6, 6, '衡力（包年）', 3, 0,
 JSON_OBJECT('phase1', JSON_OBJECT('count', 3, 'interval_days', 120)),
 3980.00, 3980.00, 3980.00,
 '2025-06-04 10:00:00', 1);
