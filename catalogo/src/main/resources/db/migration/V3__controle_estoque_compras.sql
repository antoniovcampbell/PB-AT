ALTER TABLE compras
    ADD COLUMN IF NOT EXISTS estoque_controlado BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE compras
   SET estoque_controlado = FALSE
 WHERE demonstracao = TRUE
   AND idempotency_key LIKE 'demo-avaliacoes-%';
