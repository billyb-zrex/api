CREATE DATABASE IF NOT EXISTS fable_app;
USE fable_app;

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

CREATE TABLE project (
     id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
     created_at TIMESTAMP NOT NULL,
     updated_at TIMESTAMP NOT NULL,
     name VARCHAR(255) NOT NULL,
     display_name TEXT NOT NULL,
     thumbnail TEXT
);

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

CREATE TABLE asset_mapping (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    project_id BIGINT UNSIGNED,
    name VARCHAR(500) NOT NULL,
    asset_location TEXT,
    asset_type VARCHAR(255),

    CONSTRAINT FK_project FOREIGN KEY (project_id) REFERENCES project(id)
);
CREATE INDEX asset_mapping_id_name ON asset_mapping (id, name);

