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
    index idx_gmt_modified (gmt_modified)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;