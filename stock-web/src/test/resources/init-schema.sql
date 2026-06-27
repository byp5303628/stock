-- H2 内存数据库初始化脚本 (MySQL 兼容模式)
-- 元数据表结构

CREATE TABLE IF NOT EXISTS metadata_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) DEFAULT 'DRAFT',
    ext_info VARCHAR(1000),
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_model_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS metadata_field (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id BIGINT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    business_meaning VARCHAR(500),
    required INT DEFAULT 0,
    constraints VARCHAR(500),
    enum_id BIGINT,
    sort_order INT DEFAULT 0,
    ext_info VARCHAR(1000),
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_model_field UNIQUE (model_id, field_name)
);

CREATE TABLE IF NOT EXISTS metadata_enum (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_enum_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS metadata_enum_value (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enum_id BIGINT NOT NULL,
    value_code VARCHAR(100) NOT NULL,
    value_label VARCHAR(200) NOT NULL,
    sort_order INT DEFAULT 0,
    ext_info VARCHAR(1000),
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_enum_value_code UNIQUE (enum_id, value_code)
);
