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
    thumbnail    VARCHAR(200)
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
    dp             VARCHAR(255) NULL,
    belongs_to_org BIGINT UNSIGNED,

    UNIQUE KEY UK_user_per_org (belongs_to_org, email),
    CONSTRAINT FK_user_belongs_to_org FOREIGN KEY (belongs_to_org) REFERENCES org (id)
);

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

CREATE TABLE project
(
    id             BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    rid            varchar(255) NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    display_name   VARCHAR(200) NOT NULL,
    thumbnail      VARCHAR(200),
    created_by     BIGINT UNSIGNED,
    belongs_to_org BIGINT UNSIGNED,
    no_of_screens  INT,

    CONSTRAINT FK_project_created_by_user FOREIGN KEY (created_by) REFERENCES user (id),
    CONSTRAINT FK_project_belongs_to_org FOREIGN KEY (belongs_to_org) REFERENCES org (id)
);

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

