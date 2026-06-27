-- H2 内存数据库初始化脚本 (MySQL 兼容模式)
-- 包含所有元数据表 + 股票数据表 + 任务表

-- ========== 任务调度表 ==========
CREATE TABLE IF NOT EXISTS task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_serial_no BIGINT,
    task_type VARCHAR(64) NOT NULL,
    context VARCHAR(2048) NOT NULL,
    status VARCHAR(16) NOT NULL,
    retry_times INT DEFAULT 0 NOT NULL,
    result_msg VARCHAR(256),
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fire_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS schedule_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_type VARCHAR(64) NOT NULL,
    cron_expression VARCHAR(64),
    status VARCHAR(16) NOT NULL,
    count INT,
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_task_type UNIQUE (task_type)
);

-- ========== 股票基础数据表 ==========
CREATE TABLE IF NOT EXISTS qfq_stock_basic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(16),
    name VARCHAR(64),
    start_price DECIMAL(60, 2) NOT NULL DEFAULT 0,
    end_price DECIMAL(60, 2) NOT NULL DEFAULT 0,
    highest_price DECIMAL(60, 2) NOT NULL DEFAULT 0,
    lowest_price DECIMAL(60, 2) NOT NULL DEFAULT 0,
    total_value DECIMAL(60, 2) NOT NULL DEFAULT 0,
    partition_date VARCHAR(32) NOT NULL DEFAULT '',
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hfq_stock_basic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(16),
    name VARCHAR(64),
    start_price DECIMAL(60, 2) NOT NULL DEFAULT 0,
    end_price DECIMAL(60, 2) NOT NULL DEFAULT 0,
    highest_price DECIMAL(60, 2) NOT NULL DEFAULT 0,
    lowest_price DECIMAL(60, 2) NOT NULL DEFAULT 0,
    total_value DECIMAL(60, 2) NOT NULL DEFAULT 0,
    partition_date VARCHAR(32) NOT NULL DEFAULT '',
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stock_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(16),
    name VARCHAR(64),
    ext_info VARCHAR(2048),
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_stock_info_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS stock_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(16),
    partition_date VARCHAR(32) NOT NULL,
    statistic_type VARCHAR(32) NOT NULL,
    statistics_name VARCHAR(64) NOT NULL,
    statistics VARCHAR(4096),
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS trade_policy_regression (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32),
    task_id BIGINT,
    detail VARCHAR(8192),
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tpr_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS stock_regression_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(16),
    trade_policy_name VARCHAR(32),
    trade_cycles TEXT,
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== 元数据管理表 ==========
CREATE TABLE IF NOT EXISTS metadata_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(64) NOT NULL,
    model_type VARCHAR(32) NOT NULL,
    description VARCHAR(1024) DEFAULT '',
    status VARCHAR(16) DEFAULT 'DRAFT',
    ext_info VARCHAR(2048),
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_model_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS metadata_field (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id BIGINT NOT NULL,
    field_name VARCHAR(64) NOT NULL,
    field_type VARCHAR(32) NOT NULL,
    business_meaning VARCHAR(1024) DEFAULT '',
    required INT DEFAULT 0,
    constraints VARCHAR(2048),
    enum_id BIGINT,
    sort_order INT DEFAULT 0,
    ext_info VARCHAR(2048),
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_model_field UNIQUE (model_id, field_name)
);

CREATE TABLE IF NOT EXISTS metadata_enum (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(64) NOT NULL,
    description VARCHAR(512) DEFAULT '',
    status VARCHAR(16) DEFAULT 'ENABLED',
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_enum_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS metadata_enum_value (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enum_id BIGINT NOT NULL,
    value_code VARCHAR(64) NOT NULL,
    value_label VARCHAR(128) NOT NULL,
    sort_order INT DEFAULT 0,
    ext_info VARCHAR(2048),
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_enum_value_code UNIQUE (enum_id, value_code)
);
