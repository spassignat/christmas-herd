
CREATE TABLE IF NOT EXISTS image (
    uuid         UUID          PRIMARY KEY,
    imported_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    backend      VARCHAR(100),
    animal_type  VARCHAR(20),
    content    BYTEA       NOT NULL,
    size       BIGINT      NOT NULL);
