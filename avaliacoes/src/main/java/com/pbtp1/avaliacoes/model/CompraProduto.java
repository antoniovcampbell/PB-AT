package com.pbtp1.avaliacoes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "compras_produtos", uniqueConstraints = @UniqueConstraint(columnNames = {"usuarioId", "produtoId"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraProduto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private Long produtoId;
}
