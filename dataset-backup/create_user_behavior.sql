-- ============================================================
-- 用户行为表(推荐算法数据源)
-- 记录用户浏览/收藏景点的行为, 作为协同过滤算法的输入
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_behavior` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT NOT NULL COMMENT '用户ID(关联 admin.id)',
  `spot_id` INT NOT NULL COMMENT '景点ID(关联 scenic_spot.id)',
  `behavior_type` TINYINT NOT NULL COMMENT '行为类型: 1=浏览, 2=收藏',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '行为发生时间',
  PRIMARY KEY (`id`),
  -- 同一用户对同一景点的同类型行为只记录一次, 防止重复采集
  UNIQUE KEY `uk_user_spot_type` (`user_id`, `spot_id`, `behavior_type`),
  KEY `idx_spot_id` (`spot_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表(推荐算法数据源)';
