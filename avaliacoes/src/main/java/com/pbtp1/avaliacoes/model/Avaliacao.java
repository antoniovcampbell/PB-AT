package com.pbtp1.avaliacoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "avaliacoes", uniqueConstraints = @UniqueConstraint(
        name = "uk_avaliacao_compra_produto", columnNames = {"compra_id", "produto_id"}))
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "compra_id")
    private Long compraId;

    @NotBlank
    @Column(nullable = false)
    private String nomeUsuario;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer nota;

    @Column(length = 1000)
    private String comentario;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}
