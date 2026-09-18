package com.pbtp1.shared.messaging;

public final class RabbitMQConstantes {

    private RabbitMQConstantes() {
    }

    public static final String EXCHANGE_PRODUTOS = "catalogo.produtos.exchange";
    public static final String FILA_PRODUTOS = "avaliacoes.catalogo.produtos.queue";
    public static final String EXCHANGE_COMPRAS = "catalogo.compras.exchange";
    public static final String FILA_COMPRAS = "avaliacoes.catalogo.compras.queue";

    public static final String ROUTING_KEY_PRODUTO_CRIADO = "produto.criado";
    public static final String ROUTING_KEY_PRODUTO_ATUALIZADO = "produto.atualizado";
    public static final String ROUTING_KEY_PRODUTO_EXCLUIDO = "produto.excluido";
    public static final String ROUTING_KEY_COMPRA_CRIADA = "compra.criada";
}
