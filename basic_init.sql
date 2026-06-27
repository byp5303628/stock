create table if not exists qfq_stock_basic
(
    id             bigint         not null primary key auto_increment,
    code           varchar(16),
    name           varchar(64),
    start_price    decimal(60, 2) not null default 0,
    end_price      decimal(60, 2) not null default 0,
    highest_price  decimal(60, 2) not null default 0,
    lowest_price   decimal(60, 2) not null default 0,
    total_value    decimal(60, 2) not null default 0,
    partition_date varchar(32)    not null default '',
    gmt_create     datetime                default now() not null,
    gmt_modified   datetime                default now() not null on update now(),
    unique index uniq_idx_code_partition_date (code, partition_date),
    index idx_gmt_modified (gmt_modified)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

create table if not exists hfq_stock_basic
(
    id             bigint                 not null primary key auto_increment,
    code           varchar(16),
    name           varchar(64),
    start_price    decimal(60, 2)         not null,
    end_price      decimal(60, 2)         not null,
    highest_price  decimal(60, 2)         not null,
    lowest_price   decimal(60, 2)         not null,
    total_value    decimal(60, 2)         not null,
    partition_date varchar(32)            not null,
    gmt_create     datetime default now() not null,
    gmt_modified   datetime default now() not null on update now(),
    unique index uniq_idx_code_partition_date (code, partition_date),
    index idx_gmt_modified (gmt_modified)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

create table if not exists stock_info
(
    id           bigint                 not null primary key auto_increment,
    code         varchar(16),
    name         varchar(64),
    ext_info     json,
    gmt_create   datetime default now() not null,
    gmt_modified datetime default now() not null on update now(),
    unique index uniq_idx_code (code),
    index idx_gmt_modified (gmt_modified)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

create table if not exists stock_statistics
(
    id              bigint                 not null primary key auto_increment,
    code            varchar(16),
    partition_date  varchar(32)            not null,
    statistic_type  varchar(32)            not null,
    statistics_name varchar(64)            not null,
    statistics      json,
    gmt_create      datetime default now() not null,
    gmt_modified    datetime default now() not null on update now(),
    unique index uniq_idx_code_partition_date (code, statistics_name, partition_date),
    index idx_code_partition_date_statistic_type (code, partition_date, statistic_type),
    index idx_gmt_modified (gmt_modified)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

create table if not exists trade_policy_regression
(
    id           bigint                 not null primary key auto_increment,
    name         varchar(32),
    task_id      bigint,
    detail       json,

    gmt_create   datetime default now() not null,
    gmt_modified datetime default now() not null on update now(),
    unique index uniq_idx_name (name),
    index idx_gmt_modified (gmt_modified)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

create table if not exists stock_regression_detail
(
    id                bigint                 not null primary key auto_increment,
    code              varchar(16),
    trade_policy_name varchar(32),
    trade_cycles      mediumtext,

    gmt_create        datetime default now() not null,
    gmt_modified      datetime default now() not null on update now(),
    unique index uniq_idx_code_trade_policy_name (code, trade_policy_name),
    index idx_gmt_modified (gmt_modified)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

create table if not exists metadata_model
(
    id          bigint                 not null primary key auto_increment,
    name        varchar(128)           not null comment '模型名称',
    code        varchar(64)            not null comment '模型编码',
    model_type  varchar(32)            not null comment '模型类型',
    description varchar(1024)          not null default '' comment '业务说明',
    status      varchar(16)            not null default 'DRAFT' comment '状态',
    ext_info    json                            comment '扩展属性',
    gmt_create  datetime               not null default now(),
    gmt_modified datetime              not null default now() on update now(),
    unique index uniq_idx_code (code),
    index idx_model_type (model_type),
    index idx_gmt_modified (gmt_modified)
) engine = InnoDB default charset = utf8mb4;

create table if not exists metadata_field
(
    id              bigint                 not null primary key auto_increment,
    model_id        bigint                 not null comment '所属模型',
    field_name      varchar(64)            not null comment '字段名',
    field_type      varchar(32)            not null comment '字段类型',
    business_meaning varchar(1024)         not null default '' comment '业务含义',
    required        tinyint(1)             not null default 0 comment '是否必填',
    constraints     json                            comment '约束条件',
    enum_id         bigint                          comment '绑定的枚举 ID',
    sort_order      int                    not null default 0 comment '排序',
    ext_info        json                            comment '扩展属性',
    gmt_create      datetime               not null default now(),
    gmt_modified    datetime               not null default now() on update now(),
    unique index uniq_idx_model_field (model_id, field_name),
    index idx_enum_id (enum_id),
    index idx_gmt_modified (gmt_modified)
) engine = InnoDB default charset = utf8mb4;

create table if not exists metadata_enum
(
    id          bigint                 not null primary key auto_increment,
    name        varchar(128)           not null comment '枚举名称',
    code        varchar(64)            not null comment '枚举编码',
    description varchar(512)           not null default '' comment '描述',
    status      varchar(16)            not null default 'ENABLED' comment '状态',
    gmt_create  datetime               not null default now(),
    gmt_modified datetime              not null default now() on update now(),
    unique index uniq_idx_code (code),
    index idx_gmt_modified (gmt_modified)
) engine = InnoDB default charset = utf8mb4;

create table if not exists metadata_enum_value
(
    id          bigint                 not null primary key auto_increment,
    enum_id     bigint                 not null comment '所属枚举',
    value_code  varchar(64)            not null comment '取值编码',
    value_label varchar(128)           not null comment '取值展示名',
    sort_order  int                    not null default 0 comment '排序',
    ext_info    json                            comment '扩展属性',
    gmt_create  datetime               not null default now(),
    gmt_modified datetime              not null default now() on update now(),
    unique index uniq_idx_enum_value (enum_id, value_code),
    index idx_gmt_modified (gmt_modified)
) engine = InnoDB default charset = utf8mb4;
