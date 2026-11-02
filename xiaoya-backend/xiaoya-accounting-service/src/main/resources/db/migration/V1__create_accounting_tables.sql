-- 系统维护的收支分类；第一版不开放用户自定义分类。
CREATE TABLE categories (
                            id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类 ID',
                            name VARCHAR(40) NOT NULL COMMENT '分类显示名称',
                            `type` VARCHAR(16) NOT NULL COMMENT '收支类型：EXPENSE 或 INCOME',
                            sort_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
                            active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
                            created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                            updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最近更新时间',
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_categories_type_name (`type`, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统收支分类';

-- 用户账单。user_id 不设外键，因为用户数据归用户服务管理，分属不同数据库。
CREATE TABLE bills (
                       id BIGINT NOT NULL AUTO_INCREMENT COMMENT '账单 ID',
                       user_id BIGINT NOT NULL COMMENT '所属用户 ID',
                       category_id BIGINT NOT NULL COMMENT '分类 ID',
                       transaction_type VARCHAR(16) NOT NULL COMMENT '收支类型：EXPENSE 或 INCOME',
                       amount DECIMAL(12, 2) NOT NULL COMMENT '账单金额，必须大于 0',
                       transaction_date DATE NOT NULL COMMENT '实际记账日期',
                       remark VARCHAR(500) NULL COMMENT '备注',
                       mood VARCHAR(20) NULL COMMENT '心情标签',
                       created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                       updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最近更新时间',
                       PRIMARY KEY (id),
                       KEY idx_bills_user_date (user_id, transaction_date, id),
                       KEY idx_bills_user_type_date (user_id, transaction_type, transaction_date),
                       KEY idx_bills_user_category_date (user_id, category_id, transaction_date),
                       CONSTRAINT chk_bills_transaction_type
                           CHECK (transaction_type IN ('EXPENSE', 'INCOME')),
                       CONSTRAINT chk_bills_amount_positive
                           CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账单';

-- 月预算主表；total_amount 由服务端按分类预算合计后同步维护。
CREATE TABLE monthly_budgets (
                                 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '月预算 ID',
                                 user_id BIGINT NOT NULL COMMENT '所属用户 ID',
                                 budget_month DATE NOT NULL COMMENT '预算月份，使用该月第一天表示',
                                 total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '分类预算金额之和',
                                 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最近更新时间',
                                 PRIMARY KEY (id),
                                 UNIQUE KEY uk_monthly_budgets_user_month (user_id, budget_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月预算主表';

-- 月预算分类明细；删除月预算时级联删除它的分类明细。
CREATE TABLE category_budgets (
                                  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类预算 ID',
                                  monthly_budget_id BIGINT NOT NULL COMMENT '所属月预算 ID',
                                  category_id BIGINT NOT NULL COMMENT '支出分类 ID',
                                  amount DECIMAL(12, 2) NOT NULL COMMENT '分类预算金额，必须大于 0',
                                  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最近更新时间',
                                  PRIMARY KEY (id),
                                  UNIQUE KEY uk_category_budgets_monthly_budget_category
                                      (monthly_budget_id, category_id),
                                  CONSTRAINT fk_category_budgets_monthly_budget
                                      FOREIGN KEY (monthly_budget_id) REFERENCES monthly_budgets (id)
                                          ON DELETE CASCADE,
                                  CONSTRAINT chk_category_budgets_amount_positive
                                      CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月分类预算明细';

-- 初始化第一版内置分类。不同收支类型可以有同名分类，例如“其他”。
INSERT INTO categories (name, `type`, sort_order) VALUES
                                                      ('餐饮', 'EXPENSE', 1),
                                                      ('交通', 'EXPENSE', 2),
                                                      ('购物', 'EXPENSE', 3),
                                                      ('娱乐', 'EXPENSE', 4),
                                                      ('学习', 'EXPENSE', 5),
                                                      ('医疗', 'EXPENSE', 6),
                                                      ('住房', 'EXPENSE', 7),
                                                      ('其他', 'EXPENSE', 8),
                                                      ('工资', 'INCOME', 1),
                                                      ('奖金', 'INCOME', 2),
                                                      ('兼职', 'INCOME', 3),
                                                      ('报销', 'INCOME', 4),
                                                      ('其他', 'INCOME', 5);