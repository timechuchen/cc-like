-- 创建库
create database if not exists thumb_db;

-- 切换库
use thumb_db;

-- 用户表
create table if not exists user
(
    id       bigint auto_increment
        primary key,
    username varchar(128) not null
);

-- 内容表
create table if not exists blog
(
    id         bigint auto_increment
        primary key,
    userId     bigint                             not null,
    title      varchar(512)                       null comment '标题',
    coverImg   varchar(1024)                      null comment '封面',
    content    text                               not null comment '内容',
    thumbCount int      default 0                 not null comment '点赞数',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
);
create index idx_userId
    on blog (userId);

-- 点赞记录表
create table if not exists thumb
(
    id         bigint auto_increment
        primary key,
    userId     bigint                             not null,
    blogId     bigint                             not null,
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间'
);
create unique index idx_userId_blogId
    on thumb (userId, blogId);

INSERT INTO blog (userId, title, coverImg, content, thumbCount, createTime, updateTime) VALUES
(1, '第一篇博客', 'https://example.com/cover1.jpg', '这是我的第一篇博客内容，希望大家喜欢！', 10, '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(1, '我的旅行日记', 'https://example.com/cover2.jpg', '今天去了长城，风景真美，分享给大家...', 25, '2025-01-02 14:30:00', '2025-01-02 14:30:00'),
(2, '美食推荐', 'https://example.com/cover3.jpg', '推荐一家超级好吃的火锅店，地址在...', 88, '2025-01-03 18:20:00', '2025-01-03 18:20:00'),
(3, '技术分享', 'https://example.com/cover4.jpg', '今天给大家分享一下最近学到的编程技巧...', 156, '2025-01-04 09:15:00', '2025-01-04 09:15:00'),
(2, '读书笔记', 'https://example.com/cover5.jpg', '最近在读的一本书《深入理解计算机系统》读后感...', 45, '2025-01-05 16:40:00', '2025-01-05 16:40:00');
