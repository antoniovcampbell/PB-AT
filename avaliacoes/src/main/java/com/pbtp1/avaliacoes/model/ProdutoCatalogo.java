package com.pbtp1.avaliacoes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "produto_catalogo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoCatalogo {

    @Id
    private Long id;

    private String nome;

    private String descricao;

    private Double preco;

    private Integer estoque;

    private String status;

    private Long categoriaId;

    private LocalDateTime atualizadoEm;
}
