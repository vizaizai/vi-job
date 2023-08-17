drop table if exists `sys_user`;
CREATE TABLE `sys_user` (
    `id` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT 'id',
    `user_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '用户名',
    `password` varchar(128) COLLATE utf8mb4_bin NOT NULL COMMENT '密码',
    `password_salt` varchar(128) COLLATE utf8mb4_bin NOT NULL COMMENT '密码盐值',
    `creater` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '新建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `updater` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `role` int(4) NOT NULL COMMENT '角色 1-管理员 2-普通人员',
    PRIMARY KEY (`id`),
    UNIQUE KEY `sys_user_user_name_IDX` (`user_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='系统用户';

INSERT INTO sys_user
(id, user_name, `password`, password_salt, creater, create_time, updater, update_time, `role`)
VALUES('1668158931570847711', 'admin', '32722cda40be81dd277f396fab2ea2aa', 'e5h59', 'sys', '2023-05-06 15:48:23', '', null, 1);


drop table if exists `worker`;
CREATE TABLE `worker` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
      `name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '执行器名称',
      `app_name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用名称',
      `creater` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '新建人',
      `create_time` datetime DEFAULT NULL COMMENT '创建时间',
      `updater` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新人',
      `update_time` datetime DEFAULT NULL COMMENT '更新时间',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='执行器信息';


drop table if exists `registry`;
CREATE TABLE `registry` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `worker_id` bigint(20) DEFAULT NULL COMMENT '执行器id',
    `address` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '注册地址',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `registry_address_idx` (`address`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='注册表';


drop table if exists `job`;
-- vi_job.job definition

CREATE TABLE `job` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
   `name` varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '任务名称',
   `worker_id` bigint(20) NOT NULL COMMENT '执行器id',
   `start_time` datetime DEFAULT NULL COMMENT '生命周期开始',
   `end_time` datetime DEFAULT NULL COMMENT '生命周期结束',
   `status` int(4) NOT NULL COMMENT '任务状态 0-停止 1-运行中',
   `processor_type` int(4) NOT NULL COMMENT '处理器类型 1-Bean 2-HTTP',
   `processor` varchar(128) COLLATE utf8mb4_bin NOT NULL COMMENT '处理器',
   `param` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '任务参数',
   `trigger_type` int(4) NOT NULL COMMENT '触发类型 0-非主动触发 1-cron 2-固定频率（秒）3-固定延时（秒）',
   `cron` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'cron表达式',
   `speed_s` int(4) DEFAULT NULL COMMENT '频率',
   `delayed_s` int(4) DEFAULT NULL COMMENT '延时',
   `route_type` int(4) NOT NULL COMMENT '路由策略',
   `retry_count` int(4) NOT NULL COMMENT '任务失败重试次数',
   `timeout_s` int(4) DEFAULT NULL COMMENT '任务超时时间',
   `max_wait_num` int(11) DEFAULT NULL COMMENT '单节点最大等待数量',
   `last_trigger_time` bigint(20) DEFAULT NULL COMMENT '上次次触发时间',
   `next_trigger_time` bigint(20) DEFAULT NULL COMMENT '下次触发时间',
   `log_auto_del_hours` int(11) DEFAULT NULL COMMENT '任务实例自动删除时间（小时）',
   `creater` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '新建人',
   `create_time` datetime DEFAULT NULL COMMENT '创建时间',
   `updater` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新人',
   `update_time` datetime DEFAULT NULL COMMENT '更新时间',
   PRIMARY KEY (`id`),
   KEY `job_next_trigger_time_idx` (`next_trigger_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='任务信息';


drop table if exists `job_instance`;

CREATE TABLE `job_instance` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `job_id` bigint(20) NOT NULL COMMENT '任务id',
    `job_param` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '任务参数',
    `worker_id` bigint(20) NOT NULL COMMENT '执行器id',
    `worker_address` varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '执行器地址',
    `dispatch_status` int(4) DEFAULT NULL COMMENT '调度状态 0-失败 1-调度中 2-成功',
    `execute_status` int(4) DEFAULT NULL COMMENT '执行状态 0-失败 1-执行中 2-执行成功 3-执行超时 4-执行取消',
    `error_msg` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '错误消息',
    `processor_type` int(4) NOT NULL COMMENT '处理器类型 1-Bean 2-HTTP',
    `processor` varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '处理器',
    `trigger_time` datetime DEFAULT NULL COMMENT '触发时间',
    `execute_start_time` datetime DEFAULT NULL COMMENT '执行开始时间',
    `execute_end_time` datetime DEFAULT NULL COMMENT '执行结束时间',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `expected_delete_time` datetime DEFAULT NULL COMMENT '预计删除时间',
    `exec_count` int(11) NOT NULL default 0 COMMENT '执行次数',
    PRIMARY KEY (`id`),
    KEY `trigger_time_idx` (`trigger_time`) USING BTREE,
    KEY `job_id_idx` (`job_id`) USING BTREE,
    KEY `worker_id_idx` (`worker_id`) USING BTREE,
    KEY `expected_delete_time_idx` (`expected_delete_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2513 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='任务实例';








