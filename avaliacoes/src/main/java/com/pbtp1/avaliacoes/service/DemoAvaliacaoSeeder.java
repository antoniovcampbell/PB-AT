package com.pbtp1.avaliacoes.service;

import com.pbtp1.avaliacoes.model.Avaliacao;
import com.pbtp1.avaliacoes.model.CompraProduto;
import com.pbtp1.avaliacoes.repository.AvaliacaoRepository;
import com.pbtp1.avaliacoes.repository.CompraProdutoRepository;
import com.pbtp1.avaliacoes.repository.ProdutoCatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.seed", havingValue = "true")
public class DemoAvaliacaoSeeder {
    private static final List<String> NOMES_CLIENTES = List.of(
            "Antonio Campbell", "Ana Clara Martins", "Bruno Almeida", "Camila Souza", "Daniel Oliveira",
            "Eduarda Lima", "Felipe Santos", "Giovana Rocha", "Heitor Ribeiro", "Isabela Fernandes",
            "João Pedro Costa", "Laura Nascimento", "Miguel Barros", "Nina Cardoso", "Pedro Henrique Alves");
    private static final List<String> COMENTARIOS_POR_NOTA = List.of(
            "O produto não correspondeu às minhas expectativas.",
            "Funciona, mas o acabamento poderia ser melhor.",
            "Cumpre o básico e tem qualidade razoável.",
            "Bom acabamento e fácil de usar no dia a dia.",
            "Qualidade excelente e produto igual à descrição.");

    private final CompraProdutoRepository compraProdutoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final ProdutoCatalogoRepository produtoCatalogoRepository;

    @Scheduled(initialDelayString = "${app.demo.reviews.initial-delay-ms:45000}",
            fixedDelayString = "${app.demo.reviews.fixed-delay-ms:300000}")
    @Transactional
    public void criarAvaliacoes() {
        avaliacaoRepository.deleteByCompraIdLessThan(0L);
        avaliacaoRepository.deleteByCompraIdIsNullAndComentarioStartingWith(
                "Produto comprado e aprovado na demonstração.");
        avaliacaoRepository.deleteByCompraIdIsNullAndComentarioStartingWith("Avaliação demonstrativa:");
        Map<Long, List<CompraProduto>> comprasPorProduto = new HashMap<>();
        compraProdutoRepository.findByDemonstracaoTrueAndAtivaTrueOrderByIdAsc().stream()
                .filter(compra -> compra.getCompraId() != null && produtoCatalogoRepository.existsById(compra.getProdutoId()))
                .forEach(compra -> comprasPorProduto.computeIfAbsent(compra.getProdutoId(), ignorado -> new java.util.ArrayList<>())
                        .add(compra));

        comprasPorProduto.forEach(this::criarOuAtualizarAvaliacoesDemo);
    }

    private void criarOuAtualizarAvaliacoesDemo(Long produtoId, List<CompraProduto> compras) {
        double mediaDesejada = 2.4 + Math.floorMod(produtoId.intValue() * 17, 25) / 10.0;
        for (int indice = 0; indice < compras.size(); indice++) {
            CompraProduto compra = compras.get(indice);
            String nomeUsuario = compra.getNomeUsuario() == null || compra.getNomeUsuario().isBlank()
                    ? "Cliente" : compra.getNomeUsuario();
            int nota = notaVariada(mediaDesejada, indice, compras.size());
            String comentario = COMENTARIOS_POR_NOTA.get(nota - 1);
            var existente = avaliacaoRepository.findByCompraIdAndProdutoId(compra.getCompraId(), produtoId);
            if (existente.isPresent()) {
                Avaliacao avaliacao = existente.get();
                boolean criadaPeloSeed = avaliacao.getComentario() != null
                        && (avaliacao.getComentario().startsWith("Produto comprado e aprovado na demonstração.")
                        || avaliacao.getComentario().startsWith("Avaliação demonstrativa:"));
                if (criadaPeloSeed && (avaliacao.getNota() != nota
                        || !comentario.equals(avaliacao.getComentario())
                        || !nomeUsuario.equals(avaliacao.getNomeUsuario()))) {
                    avaliacao.setNota(nota);
                    avaliacao.setComentario(comentario);
                    avaliacao.setNomeUsuario(nomeUsuario);
                    avaliacaoRepository.save(avaliacao);
                }
                continue;
            }
            avaliacaoRepository.save(Avaliacao.builder()
                    .produtoId(produtoId)
                    .compraId(compra.getCompraId())
                    .usuarioId(compra.getUsuarioId())
                    .nomeUsuario(nomeUsuario)
                    .nota(nota)
                    .comentario(comentario)
                    .build());
        }
    }

    private int notaVariada(double media, int indice, int quantidade) {
        double[] pesos = new double[5];
        double totalPeso = 0;
        for (int nota = 1; nota <= 5; nota++) {
            double distancia = nota - media;
            pesos[nota - 1] = Math.exp(-(distancia * distancia) / (2 * 1.05 * 1.05));
            totalPeso += pesos[nota - 1];
        }

        double quantil = (indice + 0.5) / quantidade;
        double acumulado = 0;
        for (int nota = 1; nota <= 5; nota++) {
            acumulado += pesos[nota - 1] / totalPeso;
            if (quantil <= acumulado) return nota;
        }
        return 5;
    }
}
