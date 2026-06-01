CREATE TABLE IF NOT EXISTS user_content (
    id              TEXT    PRIMARY KEY,
    title           TEXT    NOT NULL,
    module_name     TEXT    NOT NULL DEFAULT '',
    topic_name      TEXT    NOT NULL DEFAULT '',
    xml_content     TEXT    NOT NULL,
    base_directory  TEXT    NOT NULL,
    created_by      INTEGER NOT NULL,
    is_quiz         INTEGER NOT NULL DEFAULT 0,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (created_by) REFERENCES users(id)
)
