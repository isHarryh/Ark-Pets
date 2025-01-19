CREATE TABLE IF NOT EXISTS "metadata"
(
    "id"    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "repo"  TEXT    NOT NULL,
    "group" TEXT    NOT NULL,
    "key"   TEXT    NOT NULL,
    "value" TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS "char_info"
(
    "id"              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "asset_id"        TEXT    NOT NULL,
    "store_path"      TEXT    NOT NULL,
    "type"            TEXT    NOT NULL,
    "style"           TEXT,
    "name"            TEXT    NOT NULL,
    "appellation"     TEXT,
    "skin_group_id"   TEXT    NOT NULL,
    "skin_group_name" TEXT    NOT NULL,
    "md5"             TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS "model_assets"
(
    "id"       INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "model_id" INTEGER NOT NULL,
    "type"     TEXT    NOT NULL,
    "filename" TEXT    NOT NULL,
    "md5"      TEXT    NOT NULL DEFAULT '',
    "exist"    BOOLEAN NOT NULL DEFAULT FALSE,
    "vcs"      INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT "model_assets_fk_id" FOREIGN KEY ("model_id") REFERENCES "char_info" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS "model_tags"
(
    "id"       INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "model_id" INTEGER NOT NULL,
    "tag"      TEXT    NOT NULL,
    CONSTRAINT "model_tags_fk_id" FOREIGN KEY ("model_id") REFERENCES "char_info" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS "model_voice"
(
    "id"       INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "model_id" INTEGER NOT NULL,
    "language" TEXT    NOT NULL,
    "duration" DOUBLE  NOT NULL,
    "md5"      TEXT    NOT NULL DEFAULT '',
    "exist"    BOOLEAN NOT NULL DEFAULT FALSE,
    "vcs"      INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT "model_voice_fk_id" FOREIGN KEY ("model_id") REFERENCES "char_info" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS "model_voice_clips"
(
    "id"             INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "model_voice_id" INTEGER NOT NULL,
    "start"          DOUBLE  NOT NULL,
    "duration"       DOUBLE  NOT NULL,
    CONSTRAINT "model_voice_clips_fk_id" FOREIGN KEY ("model_voice_id") REFERENCES "model_voice" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS "metadata_index" ON "metadata" ("repo", "group", "key");
CREATE UNIQUE INDEX IF NOT EXISTS "char_info_asset_id_index" ON "char_info" ("asset_id");
CREATE INDEX IF NOT EXISTS "model_assets_model_id_index" ON "model_assets" ("model_id");
CREATE INDEX IF NOT EXISTS "model_tags_model_id_index" ON "model_tags" ("model_id");
CREATE INDEX IF NOT EXISTS "model_voice_model_id_index" ON "model_voice" ("model_id");
CREATE INDEX IF NOT EXISTS "model_voice_clips_model_voice_id_index" ON "model_voice_clips" ("model_voice_id");
