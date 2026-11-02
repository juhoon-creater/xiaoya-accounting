CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                       username VARCHAR(50) NOT NULL COMMENT '登录用户名',
                       password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt密码哈希',
                       created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                       updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='用户账号表';