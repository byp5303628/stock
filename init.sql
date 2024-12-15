create table if not exists task
(
    id                 bigint                 not null primary key auto_increment,
    external_serial_no bigint,
    task_type          varchar(64)            not null,
    context            json                   not null,
    status             varchar(16)            not null,
    retry_times        int      default 0     not null,
    result_msg         varchar(256),
    gmt_create         datetime default now() not null,
    gmt_modified       datetime default now() not null on update now(),
    fire_time          datetime default now() not null,
    index idx_task_type_status_fire_time (task_type, status, fire_time),
    index idx_task_type_external_serial_no (task_type, external_serial_no),
    index idx_gmt_modified (gmt_modified)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

create table if not exists schedule_config
(
    id              bigint                 not null primary key auto_increment,
    task_type       varchar(64)            not null,
    cron_expression varchar(64),
    status          varchar(16)            not null,
    `count`         int,
    gmt_create      datetime default now() not null,
    gmt_modified    datetime default now() not null on update now(),
    index idx_gmt_modified (gmt_modified),
    unique index uniq_idx_task_type (task_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
