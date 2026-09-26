UPDATE produto
SET version = 0
WHERE version IS NULL;

ALTER TABLE produto
    ALTER COLUMN version SET DEFAULT 0;

ALTER TABLE produto
    ALTER COLUMN version SET NOT NULL;
