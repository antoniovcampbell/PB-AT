package com.pbtp1.avaliacoes.repository;

import com.pbtp1.avaliacoes.model.Avaliacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AvaliacaoRepositoryTest {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    private Avaliacao avaliacao1;

    @BeforeEach
    void setUp() {
        avaliacao1 = avaliacaoRepository.save(
                Avaliacao.builder()
                        .produtoId(10L)
                        .nomeUsuario("Ana")
                        .nota(5)
                        .comentario("Ótimo produto")
                        .build()
        );

        avaliacaoRepository.save(
                Avaliacao.builder()
                        .produtoId(10L)
                        .nomeUsuario("Bruno")
                        .nota(4)
                        .comentario("Muito bom")
                        .build()
        );

        avaliacaoRepository.save(
                Avaliacao.builder()
                        .produtoId(20L)
                        .nomeUsuario("Carla")
                        .nota(2)
                        .comentario("Decepcionante")
                        .build()
        );
    }

    @Test
    void deveSalvarEBuscarPorId() {
        Optional<Avaliacao> encontrada = avaliacaoRepository.findById(avaliacao1.getId());
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNomeUsuario()).isEqualTo("Ana");
        assertThat(encontrada.get().getNota()).isEqualTo(5);
    }

    @Test
    void deveListarPorProduto() {
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByProdutoId(10L);
        assertThat(avaliacoes).hasSize(2);
    }

    @Test
    void deveListarPorProdutoENota() {
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByProdutoIdAndNota(10L, 4);
        assertThat(avaliacoes).hasSize(1);
        assertThat(avaliacoes.get(0).getNomeUsuario()).isEqualTo("Bruno");
    }

    @Test
    void deveListarPorNota() {
        assertThat(avaliacaoRepository.findByNota(2)).hasSize(1);
    }

    @Test
    void deveVerificarExistenciaPorProduto() {
        assertThat(avaliacaoRepository.existsByProdutoId(10L)).isTrue();
        assertThat(avaliacaoRepository.existsByProdutoId(999L)).isFalse();
    }

    @Test
    void deveCalcularMediaPorProduto() {
        Optional<Double> media = avaliacaoRepository.calcularMediaPorProduto(10L);
        assertThat(media).isPresent();
        assertThat(media.get()).isEqualTo(4.5);
    }

    @Test
    void deveContarAvaliacoesPorProduto() {
        assertThat(avaliacaoRepository.countByProdutoId(10L)).isEqualTo(2);
    }

    @Test
    void deveDeletarAvaliacao() {
        Long id = avaliacao1.getId();
        avaliacaoRepository.deleteById(id);
        assertThat(avaliacaoRepository.findById(id)).isEmpty();
    }
}
