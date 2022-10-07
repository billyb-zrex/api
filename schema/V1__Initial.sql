CREATE DATABASE IF NOT EXISTS fable_app;
USE fable_app;

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

CREATE TABLE project
(
    id             BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    name           VARCHAR(255) NOT NULL,
    display_name   TEXT         NOT NULL,
    thumbnail      TEXT,
    origin         TEXT NOT NULL,
    title          TEXT NOT NULL,
    proxyOrigin    TEXT NOT NULL
);

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

CREATE TABLE asset_mapping
(
    id           BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    project_id   BIGINT UNSIGNED,
    asset_name   VARCHAR(255) NOT NULL,
    http_status  INT          NOT NULL,
    asset_path   TEXT         NOT NULL,
    origin       TEXT         NOT NULL,
    method       VARCHAR(100),
    content_type VARCHAR(100),
    meta         JSON,

    CONSTRAINT FK_project FOREIGN KEY (project_id) REFERENCES project (id)
);

CREATE INDEX asset_mapping_id_active ON asset_mapping (project_id);
CREATE INDEX asset_mapping_id_name_active ON asset_mapping (project_id, asset_name);
-- todo no updated_at index is created, check if one is necessary

