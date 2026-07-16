CREATE TABLE IF NOT EXISTS app_users
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    username
    VARCHAR
(
    80
) NOT NULL UNIQUE, password VARCHAR
(
    100
) NOT NULL, display_name VARCHAR
(
    100
), role VARCHAR
(
    30
) NOT NULL DEFAULT 'USER', enabled BOOLEAN NOT NULL DEFAULT TRUE, avatar_url VARCHAR
(
    255
));
ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR (255);
CREATE TABLE IF NOT EXISTS knowledge_base
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    160
) NOT NULL, description VARCHAR
(
    600
), color VARCHAR
(
    20
), created_at TIMESTAMP NOT NULL);
CREATE TABLE IF NOT EXISTS knowledge_document
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    knowledge_base_id
    BIGINT
    NOT
    NULL,
    file_name
    VARCHAR
(
    255
), content_type VARCHAR
(
    120
), size BIGINT NOT NULL DEFAULT 0, segment_count INT NOT NULL DEFAULT 0, status VARCHAR
(
    30
) NOT NULL, error_message VARCHAR
(
    1000
), created_at TIMESTAMP NOT NULL);
CREATE TABLE IF NOT EXISTS chat_conversation
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    knowledge_base_id
    BIGINT
    NOT
    NULL,
    username
    VARCHAR
(
    80
) NOT NULL, title VARCHAR
(
    160
) NOT NULL, created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL);
CREATE TABLE IF NOT EXISTS chat_message
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    conversation_id
    BIGINT
    NOT
    NULL,
    role
    VARCHAR
(
    20
) NOT NULL, content TEXT NOT NULL, created_at TIMESTAMP NOT NULL);
CREATE TABLE IF NOT EXISTS knowledge_space_profile
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    knowledge_base_id
    BIGINT
    NOT
    NULL
    UNIQUE,
    owner_username
    VARCHAR
(
    80
) NOT NULL, icon VARCHAR
(
    20
) NOT NULL, sort_order INT NOT NULL DEFAULT 0, favorite BOOLEAN NOT NULL DEFAULT FALSE, archived BOOLEAN NOT NULL DEFAULT FALSE, updated_at TIMESTAMP NOT NULL);
CREATE TABLE IF NOT EXISTS knowledge_space_member
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    knowledge_base_id
    BIGINT
    NOT
    NULL,
    username
    VARCHAR
(
    80
) NOT NULL, role VARCHAR
(
    20
) NOT NULL, created_at TIMESTAMP NOT NULL, UNIQUE
(
    knowledge_base_id,
    username
));
