CREATE DATABASE IF NOT EXISTS fable_tour_app;
USE fable_tour_app;

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

CREATE TABLE org
(
    id           BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    rid          varchar(255) NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    thumbnail    VARCHAR(200),
    INDEX IDX_org (rid)
);

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

CREATE TABLE user
(
    id             BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    first_name     varchar(100) NOT NULL,
    last_name      varchar(100),
    email          varchar(255) NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    avatar         VARCHAR(255) NULL,
    belongs_to_org BIGINT UNSIGNED,

    UNIQUE KEY UK_user_per_org (belongs_to_org, email),
    CONSTRAINT FK_user_belongs_to_org FOREIGN KEY (belongs_to_org) REFERENCES org (id),
    INDEX IDX_org (belongs_to_org),
    INDEX IDX_email (email)
);

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

CREATE TABLE project
(
    id             BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    rid            VARCHAR(255) NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    display_name   VARCHAR(200) NOT NULL,
    thumbnail      VARCHAR(200),
    created_by     BIGINT UNSIGNED,
    belongs_to_org BIGINT UNSIGNED,
    no_of_screens  INT DEFAULT 0,

    CONSTRAINT FK_project_created_by_user FOREIGN KEY (created_by) REFERENCES user (id),
    CONSTRAINT FK_project_belongs_to_org FOREIGN KEY (belongs_to_org) REFERENCES org (id),
    INDEX IDX_rid (rid),
    INDEX IDX_org (belongs_to_org)
);

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

CREATE TABLE asset_proxy
(
    id              BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    rid             VARCHAR(255) NOT NULL,
    full_origin_url TEXT         NOT NULL,
    proxy_uri       VARCHAR(100) NOT NULL,
    belongs_to_proj BIGINT UNSIGNED,

    CONSTRAINT FK_proj FOREIGN KEY (belongs_to_proj) REFERENCES project (id),
    INDEX IDX_proj_rid (belongs_to_proj, rid)
);
