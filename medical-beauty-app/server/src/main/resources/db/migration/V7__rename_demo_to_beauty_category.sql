-- ============================================================================
-- V7 · 演示数据脱敏：医美 → 美容护理
--
-- 为通过微信小程序「生活服务 → 美容美发」类目审核，把 seed 数据中带有明显
-- 医疗倾向的字眼（注射剂名、抗衰、PCL/PLLA 等）改为护理通用术语。
-- 生产环境顾问可以在管理后台自行改回专业术语，对外审核截图保持安全。
-- ============================================================================

SET NAMES utf8mb4;

-- ─── 项目改名 ───────────────────────────────────────────
UPDATE `project` SET
  `name` = '焕颜光疗',
  `category` = '光疗',
  `description` = '改善皮肤色素沉淀、肤色暗沉、毛孔细化、细纹',
  `science_text` = '光疗护理技术，靶向作用于色素沉淀，激发胶原焕活。3 次为一个疗程。'
WHERE `id` = 1;

UPDATE `project` SET
  `name` = '深层补水护理',
  `category` = '护理',
  `description` = '皮肤营养补充：胶原焕颜 + 青春焕颜组合',
  `science_text` = '复合配方深层补水、强化胶原焕活，改善皮肤暗沉与干燥，提升光泽与弹性。'
WHERE `id` = 2;

UPDATE `project` SET
  `name` = '弹力紧致护理',
  `category` = '紧致',
  `description` = '皮肤紧致改善 · 弹力提升',
  `science_text` = '深层紧致护理技术，作用于皮肤深层，提升皮肤弹性，改善松弛。'
WHERE `id` = 3;

UPDATE `project` SET
  `name` = '焕颜疗程 A',
  `category` = '焕颜',
  `description` = '长效胶原焕活 · 轮廓塑形',
  `science_text` = '长效胶原焕活护理，激发自体胶原持续生成，效果维持较久。'
WHERE `id` = 4;

UPDATE `project` SET
  `name` = '焕颜疗程 B',
  `category` = '焕颜',
  `description` = '胶原焕活 · 紧致护理',
  `science_text` = '胶原焕活护理，激发胶原新生，提升整体轮廓与紧致度。'
WHERE `id` = 5;

UPDATE `project` SET
  `name` = '表情舒展护理（包年）',
  `category` = '护理',
  `description` = '表情纹舒展 · 包年套餐',
  `science_text` = '舒展过度活跃的表情肌，改善动态纹路。包年套餐覆盖全年。'
WHERE `id` = 6;

-- ─── 规划方案改文案 ─────────────────────────────────────
UPDATE `plan` SET
  `title` = '彭蕾 · 长期护理整体方案',
  `subtitle` = '面部护理 · 皮肤管理 · 长期跟踪',
  `analysis_text` = '## 面部皮肤状态分析\n\n**优势**：皮肤紧致度相对较好、五官精致，角质层健康程度良好。\n\n**关注重点**：毛孔需要细化、肤色需要提亮、色素沉淀、细纹、皮肤需要紧致护理。\n\n## 面部轮廓状态分析\n\n**关注重点**：三庭比例上庭偏短，眼周需要紧致护理；口周需要紧致；外轮廓需要细化、中面部需要提升、法令纹需要淡化、太阳穴需要饱满。'
WHERE `id` = 1;

-- ─── 章节改文案 ─────────────────────────────────────────
UPDATE `plan_section` SET `title` = '面部状态分析及护理规划' WHERE `plan_id` = 1 AND `sort` = 1;

UPDATE `plan_section` SET
  `title` = '第一步：T 区轮廓护理',
  `content` = '**1、鼻子、眉骨、印堂**\n- 鼻子：收紧鼻背及周边皮肤组织、立体支撑塑形\n- 眉骨：有效支撑眼部、上面部皮肤紧致、眼周轮廓提升\n- 印堂：鼻部和眼皮皮肤收紧、有效衔接\n\n**2、下巴下颌骨**\n- 有效支撑下颌轮廓、与下巴线条衔接、下颌线清晰'
WHERE `plan_id` = 1 AND `sort` = 2;

UPDATE `plan_section` SET
  `title` = '第二步：下庭轮廓护理',
  `content` = '**1、口周护理**：淡化法令纹、改善口角线条、收紧口周皮肤\n\n**2、唇部护理**：改善人中比例、淡化唇纹，让唇部线条更柔和'
WHERE `plan_id` = 1 AND `sort` = 3;

UPDATE `plan_section` SET
  `title` = '皮肤护理建议',
  `content` = '1. **皮肤营养补充**：胶原焕颜补水、青春焕颜补水\n2. **皮肤色素改善**：焕颜光疗\n3. **皮肤紧致改善**：弹力紧致护理'
WHERE `plan_id` = 1 AND `sort` = 4;

UPDATE `plan_section` SET
  `title` = '护理产品推荐',
  `content` = '焕颜疗程 A · 焕颜疗程 B · 胶原焕活复合物 · 立体焕颜组合'
WHERE `plan_id` = 1 AND `sort` = 5;

UPDATE `plan_section` SET `title` = '套餐组合' WHERE `plan_id` = 1 AND `sort` = 6;

-- ─── 规划项目改名（跟随 project 改名） ──────────────────
UPDATE `plan_item` SET `project_name` = '焕颜光疗'              WHERE `project_id` = 1;
UPDATE `plan_item` SET `project_name` = '深层补水护理'          WHERE `project_id` = 2;
UPDATE `plan_item` SET `project_name` = '弹力紧致护理'          WHERE `project_id` = 3;
UPDATE `plan_item` SET `project_name` = '焕颜疗程 A · 3 次'     WHERE `project_id` = 4;
UPDATE `plan_item` SET `project_name` = '焕颜疗程 B'            WHERE `project_id` = 5;
UPDATE `plan_item` SET `project_name` = '表情舒展护理（包年）'  WHERE `project_id` = 6;
