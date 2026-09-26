ALTER TABLE avaliacoes
    ADD COLUMN IF NOT EXISTS compra_id BIGINT;

UPDATE avaliacoes avaliacao
   SET compra_id = (
       SELECT compra.compra_id
         FROM compras_produtos compra
        WHERE compra.usuario_id = avaliacao.usuario_id
          AND compra.produto_id = avaliacao.produto_id
          AND compra.compra_id IS NOT NULL
        ORDER BY compra.compra_id
        LIMIT 1
   )
 WHERE avaliacao.compra_id IS NULL;

ALTER TABLE avaliacoes
    DROP CONSTRAINT IF EXISTS uk_avaliacao_usuario_produto;

ALTER TABLE avaliacoes
    ADD CONSTRAINT uk_avaliacao_compra_produto UNIQUE (compra_id, produto_id);
