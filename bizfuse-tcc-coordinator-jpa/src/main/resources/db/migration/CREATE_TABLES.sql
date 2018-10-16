CREATE TABLE IF NOT EXISTS `tcc_coordinator`.`t_participant` (
  `id`           BIGINT(19) UNSIGNED NOT NULL AUTO_INCREMENT,
  `tcc_id`       VARCHAR(36)         NOT NULL,
  `create_time`  DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_time`  DATETIME            NOT NULL DEFAULT '1970-01-01 00:00:00',
  `expire_time`  DATETIME            NOT NULL
  COMMENT '预留资源过期时间',
  `execute_time` DATETIME            NOT NULL DEFAULT '1970-01-01 00:00:00'
  COMMENT '发起confirm的时间',
  `error_code`   VARCHAR(255)        NOT NULL DEFAULT ''
  COMMENT '参与方返回的错误码',
  `uri`          VARCHAR(255)        NOT NULL
  COMMENT '预留资源回调URI',
  PRIMARY KEY (`id`),
  UNIQUE KEY (`tcc_id`, `uri`),
  INDEX `idx_participant_id` (`tcc_id` ASC)
);