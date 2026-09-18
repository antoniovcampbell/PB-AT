package com.pbtp1.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "itens_compra")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @Column(nullable = false)
    private Long produtoId;

    @Column(nullable = false)
    private String nomeProduto;

    @Column(nullable = false)
    private Double precoUnitario;

    @Column(nullable = false)
    private Integer quantidade;
}
