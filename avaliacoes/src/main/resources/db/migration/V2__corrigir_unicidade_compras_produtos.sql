DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    SELECT con.conname
      INTO constraint_name
      FROM pg_constraint con
     WHERE con.conrelid = 'compras_produtos'::regclass
       AND con.contype = 'u'
       AND cardinality(con.conkey) = 2
       AND con.conkey @> ARRAY[
           (SELECT attnum FROM pg_attribute
             WHERE attrelid = 'compras_produtos'::regclass AND attname = 'usuario_id'),
           (SELECT attnum FROM pg_attribute
             WHERE attrelid = 'compras_produtos'::regclass AND attname = 'produto_id')
       ]::SMALLINT[];

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE compras_produtos DROP CONSTRAINT %I', constraint_name);
    END IF;
END $$;
