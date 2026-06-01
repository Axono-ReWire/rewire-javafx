-- Axono ReWire schema.
-- All statements are idempotent (CREATE TABLE IF NOT EXISTS) and run once
-- on application startup by com.axono.database.SchemaInitializer.

CREATE TABLE IF NOT EXISTS users (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    username        TEXT    NOT NULL UNIQUE,
    password_hash   TEXT    NOT NULL,
    first_name      TEXT    NOT NULL,
    last_name       TEXT    NOT NULL,
    year_of_study   TEXT    NOT NULL,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS quiz_results (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL,
    presentation_id TEXT    NOT NULL,
    score           INTEGER NOT NULL,
    max_score       INTEGER NOT NULL,
    completed_at    TEXT    NOT NULL DEFAULT (datetime('now')),
    answers_json    TEXT    NOT NULL DEFAULT '[]',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS user_modules (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL,
    module_name     TEXT    NOT NULL,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(user_id, module_name)
);

CREATE TABLE IF NOT EXISTS schema_migrations (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    migration   TEXT    NOT NULL UNIQUE,
    applied_at  TEXT    NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS resource_access (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL,
    presentation_id TEXT    NOT NULL,
    module_name     TEXT    NOT NULL,
    accessed_at     TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(user_id, presentation_id)
);

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
);
