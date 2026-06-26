SET NAMES utf8mb4;

-- ============================================================================
-- 数据字典种子：核心法规域。dict_data.dict_code 存「类型编码」（如 law_type），
-- dict_value 存代码（如 law），与前端 utils/labels.ts 的 *Options.value 完全一致，
-- 故启用后前端下拉/标签直接由字典驱动、与历史硬编码常量平滑兼容。
-- 全为平台公共数据（tenant_id=0）。INSERT IGNORE 保证可重复执行。
-- ============================================================================

INSERT IGNORE INTO `lr_dict_type` (`dict_code`, `dict_name`, `status`, `remark`, `tenant_id`) VALUES
  ('law_type',          '法规类型', 'enabled', '法规文档 lawType',            0),
  ('effect_level',      '法律层级', 'enabled', '法规文档 legalLevel / 效力层级', 0),
  ('timeliness_status', '时效状态', 'enabled', '法规文档 status 时效状态',     0),
  ('region',            '适用地区', 'enabled', '省级行政区（值=中文名）',      0),
  ('version_status',    '版本状态', 'enabled', '法规版本 versionStatus',       0),
  ('revision_type',     '修订类型', 'enabled', '法规版本 revisionType',        0),
  ('relation_type',     '关系类型', 'enabled', '法规关系 relationType',        0),
  ('category_type',     '分类类型', 'enabled', '法规分类 categoryType',        0);

SET @t_law_type          = (SELECT `id` FROM `lr_dict_type` WHERE `dict_code` = 'law_type'          AND `tenant_id` = 0);
SET @t_effect_level      = (SELECT `id` FROM `lr_dict_type` WHERE `dict_code` = 'effect_level'      AND `tenant_id` = 0);
SET @t_timeliness_status = (SELECT `id` FROM `lr_dict_type` WHERE `dict_code` = 'timeliness_status' AND `tenant_id` = 0);
SET @t_region            = (SELECT `id` FROM `lr_dict_type` WHERE `dict_code` = 'region'            AND `tenant_id` = 0);
SET @t_version_status    = (SELECT `id` FROM `lr_dict_type` WHERE `dict_code` = 'version_status'    AND `tenant_id` = 0);
SET @t_revision_type     = (SELECT `id` FROM `lr_dict_type` WHERE `dict_code` = 'revision_type'     AND `tenant_id` = 0);
SET @t_relation_type     = (SELECT `id` FROM `lr_dict_type` WHERE `dict_code` = 'relation_type'     AND `tenant_id` = 0);
SET @t_category_type     = (SELECT `id` FROM `lr_dict_type` WHERE `dict_code` = 'category_type'     AND `tenant_id` = 0);

-- 法规类型
INSERT IGNORE INTO `lr_dict_data` (`dict_type_id`, `dict_code`, `dict_label`, `dict_value`, `sort_order`, `status`, `tenant_id`) VALUES
  (@t_law_type, 'law_type', '法律',     'law',        1, 'enabled', 0),
  (@t_law_type, 'law_type', '行政法规', 'regulation', 2, 'enabled', 0),
  (@t_law_type, 'law_type', '部门规章', 'rule',       3, 'enabled', 0),
  (@t_law_type, 'law_type', '规范性文件', 'normative', 4, 'enabled', 0),
  (@t_law_type, 'law_type', '国家标准', 'standard',   5, 'enabled', 0),
  (@t_law_type, 'law_type', '政策',     'policy',     6, 'enabled', 0);

-- 法律层级（效力层级）
INSERT IGNORE INTO `lr_dict_data` (`dict_type_id`, `dict_code`, `dict_label`, `dict_value`, `sort_order`, `status`, `tenant_id`) VALUES
  (@t_effect_level, 'effect_level', '法律',     'law',        1, 'enabled', 0),
  (@t_effect_level, 'effect_level', '行政法规', 'regulation', 2, 'enabled', 0),
  (@t_effect_level, 'effect_level', '部门规章', 'rule',       3, 'enabled', 0),
  (@t_effect_level, 'effect_level', '规范性文件', 'normative', 4, 'enabled', 0),
  (@t_effect_level, 'effect_level', '国家标准', 'standard',   5, 'enabled', 0);

-- 时效状态
INSERT IGNORE INTO `lr_dict_data` (`dict_type_id`, `dict_code`, `dict_label`, `dict_value`, `sort_order`, `status`, `tenant_id`) VALUES
  (@t_timeliness_status, 'timeliness_status', '现行有效', 'effective',     1, 'enabled', 0),
  (@t_timeliness_status, 'timeliness_status', '已修改',   'amended',       2, 'enabled', 0),
  (@t_timeliness_status, 'timeliness_status', '尚未生效', 'not_effective', 3, 'enabled', 0),
  (@t_timeliness_status, 'timeliness_status', '已失效',   'expired',       4, 'enabled', 0),
  (@t_timeliness_status, 'timeliness_status', '已废止',   'repealed',      5, 'enabled', 0),
  (@t_timeliness_status, 'timeliness_status', '未知',     'unknown',       6, 'enabled', 0);

-- 适用地区（省级行政区，值=中文名，与 FlkCodeTables.REGION_BY_ZDJG / labels.ts regionOptions 同步）
INSERT IGNORE INTO `lr_dict_data` (`dict_type_id`, `dict_code`, `dict_label`, `dict_value`, `sort_order`, `status`, `tenant_id`) VALUES
  (@t_region, 'region', '北京市',           '北京市',           1,  'enabled', 0),
  (@t_region, 'region', '天津市',           '天津市',           2,  'enabled', 0),
  (@t_region, 'region', '河北省',           '河北省',           3,  'enabled', 0),
  (@t_region, 'region', '山西省',           '山西省',           4,  'enabled', 0),
  (@t_region, 'region', '内蒙古自治区',     '内蒙古自治区',     5,  'enabled', 0),
  (@t_region, 'region', '辽宁省',           '辽宁省',           6,  'enabled', 0),
  (@t_region, 'region', '吉林省',           '吉林省',           7,  'enabled', 0),
  (@t_region, 'region', '黑龙江省',         '黑龙江省',         8,  'enabled', 0),
  (@t_region, 'region', '上海市',           '上海市',           9,  'enabled', 0),
  (@t_region, 'region', '江苏省',           '江苏省',           10, 'enabled', 0),
  (@t_region, 'region', '浙江省',           '浙江省',           11, 'enabled', 0),
  (@t_region, 'region', '安徽省',           '安徽省',           12, 'enabled', 0),
  (@t_region, 'region', '福建省',           '福建省',           13, 'enabled', 0),
  (@t_region, 'region', '江西省',           '江西省',           14, 'enabled', 0),
  (@t_region, 'region', '山东省',           '山东省',           15, 'enabled', 0),
  (@t_region, 'region', '河南省',           '河南省',           16, 'enabled', 0),
  (@t_region, 'region', '湖北省',           '湖北省',           17, 'enabled', 0),
  (@t_region, 'region', '湖南省',           '湖南省',           18, 'enabled', 0),
  (@t_region, 'region', '广东省',           '广东省',           19, 'enabled', 0),
  (@t_region, 'region', '广西壮族自治区',   '广西壮族自治区',   20, 'enabled', 0),
  (@t_region, 'region', '海南省',           '海南省',           21, 'enabled', 0),
  (@t_region, 'region', '重庆市',           '重庆市',           22, 'enabled', 0),
  (@t_region, 'region', '四川省',           '四川省',           23, 'enabled', 0),
  (@t_region, 'region', '贵州省',           '贵州省',           24, 'enabled', 0),
  (@t_region, 'region', '云南省',           '云南省',           25, 'enabled', 0),
  (@t_region, 'region', '西藏自治区',       '西藏自治区',       26, 'enabled', 0),
  (@t_region, 'region', '陕西省',           '陕西省',           27, 'enabled', 0),
  (@t_region, 'region', '甘肃省',           '甘肃省',           28, 'enabled', 0),
  (@t_region, 'region', '青海省',           '青海省',           29, 'enabled', 0),
  (@t_region, 'region', '宁夏回族自治区',   '宁夏回族自治区',   30, 'enabled', 0),
  (@t_region, 'region', '新疆维吾尔自治区', '新疆维吾尔自治区', 31, 'enabled', 0);

-- 版本状态
INSERT IGNORE INTO `lr_dict_data` (`dict_type_id`, `dict_code`, `dict_label`, `dict_value`, `sort_order`, `status`, `tenant_id`) VALUES
  (@t_version_status, 'version_status', '草稿',   'draft',     1, 'enabled', 0),
  (@t_version_status, 'version_status', '审核中', 'auditing',  2, 'enabled', 0),
  (@t_version_status, 'version_status', '已发布', 'published', 3, 'enabled', 0),
  (@t_version_status, 'version_status', '已下线', 'offline',   4, 'enabled', 0);

-- 修订类型
INSERT IGNORE INTO `lr_dict_data` (`dict_type_id`, `dict_code`, `dict_label`, `dict_value`, `sort_order`, `status`, `tenant_id`) VALUES
  (@t_revision_type, 'revision_type', '初始颁布', 'initial',  1, 'enabled', 0),
  (@t_revision_type, 'revision_type', '修订',     'revised',  2, 'enabled', 0),
  (@t_revision_type, 'revision_type', '修正',     'amended',  3, 'enabled', 0),
  (@t_revision_type, 'revision_type', '废止',     'repealed', 4, 'enabled', 0);

-- 关系类型
INSERT IGNORE INTO `lr_dict_data` (`dict_type_id`, `dict_code`, `dict_label`, `dict_value`, `sort_order`, `status`, `tenant_id`) VALUES
  (@t_relation_type, 'relation_type', '修订', 'amend',     1, 'enabled', 0),
  (@t_relation_type, 'relation_type', '废止', 'repeal',    2, 'enabled', 0),
  (@t_relation_type, 'relation_type', '引用', 'cite',      3, 'enabled', 0),
  (@t_relation_type, 'relation_type', '解释', 'interpret', 4, 'enabled', 0),
  (@t_relation_type, 'relation_type', '配套', 'support',   5, 'enabled', 0),
  (@t_relation_type, 'relation_type', '冲突', 'conflict',  6, 'enabled', 0);

-- 分类类型
INSERT IGNORE INTO `lr_dict_data` (`dict_type_id`, `dict_code`, `dict_label`, `dict_value`, `sort_order`, `status`, `tenant_id`) VALUES
  (@t_category_type, 'category_type', '主题',     'subject',     1, 'enabled', 0),
  (@t_category_type, 'category_type', '地区',     'region',      2, 'enabled', 0),
  (@t_category_type, 'category_type', '行业',     'industry',    3, 'enabled', 0),
  (@t_category_type, 'category_type', '效力层级', 'legal_level', 4, 'enabled', 0);
