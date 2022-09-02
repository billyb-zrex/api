CREATE DATABASE IF NOT EXISTS fable_app;
USE fable_app;

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

CREATE TABLE project
(
    id           BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    name         VARCHAR(255) NOT NULL,
    display_name TEXT         NOT NULL,
    thumbnail    TEXT
);

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

CREATE TABLE asset_mapping
(
    id           BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL,
    project_id   BIGINT UNSIGNED,
    asset_path   TEXT      NOT NULL,
    is_active    bool      NOT NULL DEFAULT true,
    origin       TEXT      NOT NULL,
    http_status  int       NOT NULL,
    location     TEXT,
    method       VARCHAR(100),
    content_type VARCHAR(100),
    query_params JSON,
    req_headers  JSON,
    resp_headers JSON,
    meta         JSON,

    CONSTRAINT FK_project FOREIGN KEY (project_id) REFERENCES project (id)
);

CREATE INDEX asset_mapping_id_active ON asset_mapping (project_id, is_active);

