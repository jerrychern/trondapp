CREATE TABLE IF NOT EXISTS `withdraw_fail_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `round_number` int(50) DEFAULT NULL COMMENT '轮次号',
  `withdraw_type` int(2) DEFAULT NULL COMMENT '提现类型: 1 普通提现; 2 大奖提现',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='提现失败日志表';

CREATE TABLE IF NOT EXISTS `secret` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `round_number` int(50) DEFAULT NULL COMMENT '轮次号,唯一索引',
  `key` varchar(256) DEFAULT NULL COMMENT '原始秘钥',
  PRIMARY KEY (`id`),
  UNIQUE KEY `round_number` (`round_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='秘钥信息表';