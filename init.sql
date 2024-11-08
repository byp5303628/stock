create table if not exists task
(
    id           bigint      not null primary key auto_increment,
    task_type    varchar(64) not null,
    context      json        not null,
    status       varchar(16) not null,
    retry_times  int      default 0,
    error_msg    varchar(256),
    gmt_create   datetime default now(),
    gmt_modified datetime default now() on update now(),
    fire_time    datetime default now(),
    index idx_task_type_status_fire_time (task_type, status, fire_time),
    index idx_gmt_modified (gmt_modified)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;