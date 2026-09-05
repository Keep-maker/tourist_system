-- ============================================================
-- 1. admin 表增加手机号、头像字段(个人中心用)
-- ============================================================
ALTER TABLE `admin` ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '手机号' AFTER `password`;
ALTER TABLE `admin` ADD COLUMN `avatar` VARCHAR(255) NULL COMMENT '头像URL' AFTER `phone`;

-- ============================================================
-- 2. 景点评论表
-- ============================================================
CREATE TABLE IF NOT EXISTS `spot_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spot_id` INT NOT NULL COMMENT '景点ID(关联 scenic_spot.id)',
  `user_id` INT NOT NULL COMMENT '评论用户ID(关联 admin.id)',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `rating` TINYINT NOT NULL DEFAULT 5 COMMENT '评分(1-5星)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_spot_id` (`spot_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点评论表';
