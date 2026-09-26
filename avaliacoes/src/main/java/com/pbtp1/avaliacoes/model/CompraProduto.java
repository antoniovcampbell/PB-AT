package com.pbtp1.avaliacoes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "compras_produtos", uniqueConstraints = @UniqueConstraint(
        name = "uk_compra_produto_compra_produto", columnNames = {"compra_id", "produto_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraProduto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compra_id")
    private Long compraId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "nome_usuario")
    private String nomeUsuario;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativa = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean demonstracao = false;
}
