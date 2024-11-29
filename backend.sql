-- 创建 `role_permission` 表
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE role_permission (
    role_id INT,
    role_name VARCHAR(50),
    permission_id INT,
    permission_name VARCHAR(50),
    rate1 DOUBLE DEFAULT 0.0,
    rate2 DOUBLE DEFAULT 0.0,
    is_enabled BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (role_id, permission_id)
);

INSERT INTO role_permission (role_id, role_name, permission_id, permission_name, rate1, rate2, is_enabled) VALUES
    (1, '管理员', 1, '一级抽佣', 0.95, 0.3, FALSE),
    (1, '管理员', 2, '二级抽佣', 0.95, 0.1, FALSE),
    (1, '管理员', 3, '三级抽佣', 0.95, 0, FALSE),
    (2, '主管', 1, '一级抽佣', 0.95, 0.3, FALSE),
    (2, '主管', 2, '二级抽佣', 0.95, 0.1, FALSE),
    (2, '主管', 3, '三级抽佣', 0.95, 0, FALSE),
    (3, '销售', 1, '一级抽佣', 0.95, 0.3, FALSE),
    (3, '销售', 2, '二级抽佣', 0.95, 0.1, FALSE),
    (3, '销售', 3, '三级抽佣', 0.95, 0, FALSE),
    (4, '高阶学员', 1, '一级抽佣', 0.95, 0.3, FALSE),
    (4, '高阶学员', 2, '二级抽佣', 0.95, 0.1, FALSE),
    (4, '高阶学员', 3, '三级抽佣', 0.95, 0, FALSE),
    (5, '中阶学员', 1, '一级抽佣', 0.95, 0.3, FALSE),
    (5, '中阶学员', 2, '二级抽佣', 0.95, 0.1, FALSE),
    (5, '中阶学员', 3, '三级抽佣', 0.95, 0, FALSE),
    (6, '初阶学员', 1, '一级抽佣', 0.95, 0.3, FALSE),
    (6, '初阶学员', 2, '二级抽佣', 0.95, 0.1, FALSE),
    (6, '初阶学员', 3, '三级抽佣', 0.95, 0, FALSE),
    (7, '投手', 1, '一级抽佣', 0.95, 0.3, FALSE),
    (7, '投手', 2, '二级抽佣', 0.95, 0.1, FALSE),
    (7, '投手', 3, '三级抽佣', 0.95, 0, FALSE),
    (8, '财务', 1, '一级抽佣', 0.95, 0.3, FALSE),
    (8, '财务', 2, '二级抽佣', 0.95, 0.1, FALSE),
    (8, '财务', 3, '三级抽佣', 0.95, 0, FALSE),
    (9, '导师', 1, '一级抽佣', 0.95, 0.3, FALSE),
    (9, '导师', 2, '二级抽佣', 0.95, 0.1, FALSE),
    (9, '导师', 3, '三级抽佣', 0.95, 0, FALSE);

-- 创建 `backend_user` 表，并设置 `AUTO_INCREMENT` 初始值
DROP TABLE IF EXISTS `backend_user`;
CREATE TABLE backend_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fullname VARCHAR(100),
    region_name VARCHAR(50),
    currency_name VARCHAR(50),
    platform_id BIGINT,
    status BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) AUTO_INCREMENT = 3689;

-- 创建 `role_permission_relationship` 表
DROP TABLE IF EXISTS role_permission_relationship;
CREATE TABLE role_permission_relationship (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    permission_id INT NOT NULL,
    permission_name VARCHAR(50) NOT NULL,
    rate1 DOUBLE DEFAULT 0.0,
    rate2 DOUBLE DEFAULT 0.0,
    start_date DATE NOT NULL,
    end_date DATE,
    status BOOLEAN NOT NULL DEFAULT FALSE,
    creater_id BIGINT NOT NULL
);

-- 创建 `tiktok_relationship` 表
DROP TABLE IF EXISTS tiktok_relationship;
CREATE TABLE tiktok_relationship (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tiktok_id VARCHAR(50),
    tiktok_account VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    status BOOLEAN NOT NULL DEFAULT FALSE,
    creater_id BIGINT NOT NULL,
    sync_at DATETIME
);

-- 创建 `creater_relationship` 表
DROP TABLE IF EXISTS creater_relationship;
CREATE TABLE creater_relationship (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    creater_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE
);

-- 创建 `inviter_relationship` 表
DROP TABLE IF EXISTS inviter_relationship;
CREATE TABLE inviter_relationship (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    inviter_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    status BOOLEAN NOT NULL DEFAULT FALSE,
    creater_id BIGINT NOT NULL
);

-- 创建 `manager_relationship` 表
DROP TABLE IF EXISTS manager_relationship;
CREATE TABLE manager_relationship (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    manager_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    status BOOLEAN NOT NULL DEFAULT FALSE,
    creater_id BIGINT NOT NULL
);

-- 创建 `teacher_relationship` 表
DROP TABLE IF EXISTS teacher_relationship;
CREATE TABLE teacher_relationship (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    status BOOLEAN NOT NULL DEFAULT FALSE,
    creater_id BIGINT NOT NULL
);

-- 15. 插入初始管理员用户
INSERT INTO backend_user 
    (username, password, status) 
VALUES
    ('etasha', '$2b$12$XyztwxNBtInUH5WlK4UXMuqFmBLDWlrKvDmM.npDmKlqS1GwgtOoe', TRUE);

INSERT INTO creater_ralaionship (user_id, creater_id, start_date) VALUES
    (3689, 3689, '2021-01-01');

INSERT INTO role_permission_relationship (user_id, role_id, permission_id, rate1, rate2, start_date, status, creater_id) VALUES
    (3689, 1, 1, 0.95, 0.3, '2021-01-01', TRUE, 3689),
    (3689, 1, 2, 0.95, 0.1, '2021-01-01', TRUE, 3689),
    (3689, 1, 3, 0.95, 0, '2021-01-01', TRUE, 3689);

-- 创建 `application_process_record` 表
DROP TABLE IF EXISTS `application_process_record`;
CREATE TABLE application_process_record (
    process_id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 自增ID
    user_id BIGINT, -- 学员ID
    username VARCHAR(100), -- 学员姓名
    fullname VARCHAR(100) NOT NULL UNIQUE, -- 学员全名
    platform_id BIGINT, -- 平台ID
    role_id INT NOT NULL, -- 角色ID
    inviter_id BIGINT, -- 邀请人ID
    inviter_name VARCHAR(100) NOT NULL, -- 邀请人姓名
    manager_id BIGINT, -- 经理ID
    creater_id BIGINT NOT NULL, -- 创建人ID
    rateA VARCHAR(50), -- 一级提成比例
    rateB VARCHAR(50), -- 二级提成比例
    start_date DATE NOT NULL, -- 生效日期
    tiktok_account VARCHAR(100), -- TikTok账号
    region_name VARCHAR(50) NOT NULL, -- 地区名称
    currency_name VARCHAR(50) NOT NULL, -- 货币类型
    currency_code VARCHAR(10) NOT NULL, -- 货币代码
    project_name VARCHAR(100) NOT NULL, -- 项目名称
    project_amount DOUBLE NOT NULL DEFAULT 0.0, -- 项目金额
    payment_method VARCHAR(50) NOT NULL, -- 缴款方式
    process_status INT NOT NULL, -- 流程状态
    comments TEXT, -- 备注
    action_str TEXT, -- 操作记录
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP -- 创建时间
);

-- 创建 `application_payment_record` 表
DROP TABLE IF EXISTS `application_payment_record`;
CREATE TABLE application_payment_record (
    payment_id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 自增ID
    process_id BIGINT NOT NULL, -- 关联的流程单ID
    region_name VARCHAR(50) NOT NULL, -- 地区名称
    currency_name VARCHAR(50) NOT NULL, -- 货币类型
    currency_code VARCHAR(10) NOT NULL, -- 货币代码
    project_name VARCHAR(100) NOT NULL, -- 项目名称
    project_amount DOUBLE NOT NULL DEFAULT 0.0, -- 项目金额
    project_currency_name VARCHAR(50) NOT NULL, -- 项目货币名称
    project_currency_code VARCHAR(10) NOT NULL, -- 项目货币代码
    payment_method VARCHAR(50) NOT NULL, -- 缴款方式
    payment_amount DOUBLE NOT NULL DEFAULT 0.0, -- 缴款金额
    fee DOUBLE NOT NULL DEFAULT 0.0, -- 手续费
    actual DOUBLE NOT NULL DEFAULT 0.0, -- 实际到账金额
    payment_date DATE NOT NULL, -- 缴款时间
    payment_account_id BIGINT, -- 缴款账户ID
    payment_account_str TEXT, -- 缴款账户连接字符
    creater_id BIGINT NOT NULL, -- 创建人ID
    creater_name VARCHAR(100) NOT NULL, -- 创建人姓名
    creater_fullname VARCHAR(100), -- 创建人全名
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    finance_id BIGINT, -- 财务人员ID
    finance_name VARCHAR(100), -- 财务人员姓名
    finance_fullname VARCHAR(100), -- 财务人员全名
    finance_approval_time DATETIME, -- 财务审核时间
    comments TEXT, -- 备注
    status BOOLEAN NOT NULL DEFAULT FALSE -- 缴款状态
);

-- 创建 `application_flow_record` 表
DROP TABLE IF EXISTS `application_flow_record`;
CREATE TABLE application_flow_record (
    flow_id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 自增ID
    process_id BIGINT NOT NULL, -- 关联的流程单ID
    action VARCHAR(50) NOT NULL, -- 操作类型（如 SUBMIT, WITHDRAW, CANCEL, APPROVE 等）
    creater_id BIGINT NOT NULL, -- 执行操作的用户ID
    creater_name VARCHAR(100) NOT NULL, -- 执行操作的用户姓名
    creater_fullname VARCHAR(100), -- 执行操作的用户全名
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 操作时间
    comments TEXT -- 备注（可选）
);

-- 创建 `payment_account` 表
DROP TABLE IF EXISTS `payment_account`;
CREATE TABLE payment_account (
    account_id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 自增ID
    account_name VARCHAR(100) NOT NULL, -- 账户名称
    account_number VARCHAR(100), -- 账户号码
    account_type VARCHAR(50), -- 账户类型（如银行卡、支付宝、微信等）
    account_bank VARCHAR(100), -- 开户行
    account_holder VARCHAR(100), -- 开户人
    account_currency VARCHAR(50), -- 账户币种
    account_currency_code VARCHAR(10), -- 账户币种代码
    account_region VARCHAR(50), -- 账户地区
    account_status BOOLEAN NOT NULL DEFAULT FALSE, -- 账户状态
    account_comments TEXT, -- 备注
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 更新时间
);

INSERT INTO payment_account (account_name, account_number, account_type, account_bank, account_holder, account_currency, account_currency_code, account_region, account_status, account_comments) VALUES
    ('未知', '', '', '', '', '', '', '', TRUE, ''),
    ('中国', '6217 0032 1001 1234 567', '银行卡', '中国银行', '张三', '人民币', 'CNY', '中国', TRUE, '测试数据'),
    ('马来西亚', '1234 5678 9012 3456', '银行卡', '马来西亚银行', '李四', '马来西亚令吉', 'MYR', '马来西亚', TRUE, '测试数据'),
    ('美国', '1234 5678 9012 3456', '银行卡', '美国银行', '王五', '美元', 'USD', '美国', TRUE, '测试数据'),
    ('英国', '1234 5678 9012 3456', '银行卡', '英国银行', '赵六', '英镑', 'GBP', '英国', TRUE, '测试数据'),
    ('加拿大', '1234 5678 9012 3456', '银行卡', '加拿大银行', '钱七', '加币', 'CAD', '加拿大', TRUE, '测试数据'),
    ('澳大利亚', '1234 5678 9012 3456', '银行卡', '澳大利亚银行', '孙八', '澳元', 'AUD', '澳大利亚', TRUE, '测试数据'),
    ('新西兰', '1234 5678 9012 3456', '银行卡', '新西兰银行', '周九', '新西兰元', 'NZD', '新西兰', TRUE, '测试数据'),
    ('瑞士', '1234 5678 9012 3456', '银行卡', '瑞士银行', '吴十', '瑞士法郎', 'CHF', '瑞士', TRUE, '测试数据');

-- 创建 `tiktok_user_details` 表
DROP TABLE IF EXISTS `tiktok_user_details`;
CREATE TABLE tiktok_user_details (
    tiktok_id VARCHAR(50) PRIMARY KEY, -- TikTok平台的用户ID
    tiktok_account VARCHAR(100), -- TikTok账号
    unique_id VARCHAR(50), -- TikTok平台的唯一用户ID
    nickname VARCHAR(100), -- 用户昵称
    avatar_larger TEXT, -- 大图头像URL
    avatar_medium TEXT, -- 中图头像URL
    avatar_thumb TEXT, -- 小图头像URL
    signature TEXT, -- 用户签名
    verified BOOLEAN, -- 是否认证
    sec_uid VARCHAR(255), -- 安全UID
    private_account BOOLEAN, -- 是否为私密账户
    following_visibility INT, -- 关注可见性设置
    comment_setting INT, -- 评论设置
    duet_setting INT, -- 合拍设置
    stitch_setting INT, -- 拼接设置
    download_setting INT, -- 下载设置
    profile_embed_permission INT, -- 个人资料嵌入权限
    profile_tab_show_playlist_tab BOOLEAN, -- 是否展示播放列表标签
    commerce_user BOOLEAN, -- 是否为商业用户
    tt_seller BOOLEAN, -- 是否为TikTok卖家
    relation INT, -- 用户关系
    is_ad_virtual BOOLEAN, -- 是否为虚拟广告用户
    is_embed_banned BOOLEAN, -- 是否禁用嵌入
    open_favorite BOOLEAN, -- 是否开放收藏
    nick_name_modify_time BIGINT, -- 昵称修改时间戳
    can_exp_playlist BOOLEAN, -- 是否可以播放列表
    secret Boolean, -- 是否为私密账户
    ftc Boolean, -- 是否为FTC
    link TEXT, -- 用户链接
    risk BIGINT, -- 风险等级

    -- 统计信息字段
    digg_count INT, -- 点赞数
    follower_count INT, -- 粉丝数
    following_count INT, -- 关注数
    friend_count INT, -- 好友数
    heart_count INT, -- 获赞总数
    video_count INT, -- 视频总数

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 记录创建时间
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 记录更新时间
    comments VARCHAR(64) -- 备注
);

-- 创建 `tiktok_video_details` 表
DROP TABLE IF EXISTS `tiktok_video_details`;
CREATE TABLE tiktok_video_details (
    tiktok_video_id VARCHAR(50) PRIMARY KEY, -- TikTok平台的唯一视频ID
    author_id VARCHAR(50) NOT NULL, -- 视频作者ID
    AIGCDescription TEXT,
    categoryType INT,
    backendSourceEventTracking TEXT,
    collected BOOLEAN,
    createTime BIGINT,
    video_desc TEXT,
    digged BOOLEAN,
    diversificationId INT,
    duetDisplay INT,
    duetEnabled BOOLEAN,
    forFriend BOOLEAN,
    itemCommentStatus INT,
    officalItem BOOLEAN,
    originalItem BOOLEAN,
    privateItem BOOLEAN,
    secret BOOLEAN,
    shareEnabled BOOLEAN,
    stitchDisplay INT,
    stitchEnabled BOOLEAN,

    -- item_control
    can_repost BOOLEAN,

    -- statsV2
    collectCount INT,
    commentCount INT,
    diggCount INT,
    playCount INT,
    repostCount INT,
    shareCount INT,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建 `usd_rate` 表
DROP TABLE IF EXISTS `usd_rate`;
CREATE TABLE usd_rate (
    rate_id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 自增ID
    currency_code VARCHAR(10) NOT NULL, -- 货币类型
    rate DOUBLE NOT NULL, -- 汇率
    date DATE NOT NULL, -- 日期
    INDEX idx_date_currency (date, currency_code)
);

-- 创建 `project` 表
DROP TABLE IF EXISTS `project`;
CREATE TABLE project (
    project_id INT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    project_name VARCHAR(50) NOT NULL,
    project_amount DOUBLE NOT NULL DEFAULT 0.0
);

INSERT INTO project (project_id, role_id, project_name, project_amount) VALUES
(1, 4, '高阶课程1', 38888),
(2, 5, '中阶课程', 8888),
(3, 6, '初阶课程', 998),
(4, 4, '分公司', 58888);

-- 创建 `region_project` 表
DROP TABLE IF EXISTS `region_project`;
CREATE TABLE region_project (
    region_code INT NOT NULL,
    region_name VARCHAR(50) NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    currency_name VARCHAR(50) NOT NULL,
    project_id INT NOT NULL,
    project_name VARCHAR(50) NOT NULL,
    project_amount DOUBLE NOT NULL DEFAULT 0.0,
    PRIMARY KEY (region_name, currency_code, project_id)
);

INSERT INTO region_project (region_code, region_name, currency_code, currency_name, project_id, project_name, project_amount) VALUES
(1, '美国', 'USD', '美元', '1', '高阶课程', 38888),
(1, '美国', 'USD', '美元', '2', '中阶课程', 9998),
(1, '美国', 'USD', '美元', '3', '初阶课程', 998),
(1, '美国', 'USD', '美元', '4', '分公司', 58888),
(1, '加拿大', 'CAD', '加币', '1', '高阶课程', 54766),
(1, '加拿大', 'CAD', '加币', '2', '中阶课程', 14080),
(1, '加拿大', 'CAD', '加币', '3', '初阶课程', 1400),
(1, '加拿大', 'CAD', '加币', '4', '分公司', 82932),
(44, '英国', 'USD', '美元', '1', '高阶课程', 38888),
(44, '英国', 'USD', '美元', '2', '中阶课程', 8888),
(44, '英国', 'USD', '美元', '3', '初阶课程', 998),
(60, '马来西亚', 'MYR', '令吉', '1', '高阶课程', 163300),
(60, '马来西亚', 'MYR', '令吉', '2', '中阶课程', 22650),
(60, '马来西亚', 'MYR', '令吉', '3', '初阶课程', 2600),
(86, '中国', 'CNY', '人民币', '1', '高阶课程', 281540),
(86, '中国', 'CNY', '人民币', '2', '中阶课程', 72383),
(86, '中国', 'CNY', '人民币', '3', '初阶课程', 7225),
(86, '中国', 'CNY', '人民币', '4', '分公司', 426335),
(886, '台湾', 'TWD', '新台币', '1', '高阶课程', 1188888),
(886, '台湾', 'TWD', '新台币', '2', '中阶课程', 164888),
(886, '台湾', 'TWD', '新台币', '3', '初阶课程', 18888);

-- 创建 `region_currency` 表
DROP TABLE IF EXISTS `region_currency`;
CREATE TABLE region_currency (
    region_name VARCHAR(50) PRIMARY KEY,
    region_code INT NOT NULL,
    currency_name VARCHAR(50) NOT NULL,
    currency_code VARCHAR(10) NOT NULL
);

INSERT INTO region_currency (region_name, region_code, currency_name, currency_code) VALUES
('美国', 1, '美元', 'USD'),
('欧元区', 0, '欧元', 'EUR'),
('日本', 81, '日元', 'JPY'),
('英国', 44, '英镑', 'GBP'),
('中国', 86, '人民币', 'CNY'),
('台湾', 886, '新台币', 'TWD'),
('加拿大', 1, '加币', 'CAD'),
('澳大利亚', 61, '澳元', 'AUD'),
('新西兰', 64, '新西兰元', 'NZD'),
('瑞士', 41, '瑞士法郎', 'CHF'),
('韩国', 82, '韩元', 'KRW'),
('印度', 91, '印度卢比', 'INR'),
('俄罗斯', 7, '卢布', 'RUB'),
('巴西', 55, '巴西雷亚尔', 'BRL'),
('南非', 27, '南非兰特', 'ZAR'),
('墨西哥', 52, '墨西哥比索', 'MXN'),
('新加坡', 65, '新加坡元', 'SGD'),
('香港', 852, '港元', 'HKD'),
('土耳其', 90, '土耳其里拉', 'TRY'),
('挪威', 47, '挪威克朗', 'NOK'),
('瑞典', 46, '瑞典克朗', 'SEK'),
('丹麦', 45, '丹麦克朗', 'DKK'),
('波兰', 48, '波兰兹罗提', 'PLN'),
('匈牙利', 36, '匈牙利福林', 'HUF'),
('捷克', 420, '捷克克朗', 'CZK'),
('马来西亚', 60, '令吉', 'MYR'),
('印度尼西亚', 62, '印尼卢比', 'IDR'),
('泰国', 66, '泰铢', 'THB'),
('越南', 84, '越南盾', 'VND'),
('菲律宾', 63, '菲律宾比索', 'PHP');

