package com.pbtp1.avaliacoes.service;

import com.pbtp1.avaliacoes.model.Avaliacao;
import com.pbtp1.avaliacoes.repository.AvaliacaoRepository;
import com.pbtp1.avaliacoes.repository.CompraProdutoRepository;
import com.pbtp1.avaliacoes.repository.ProdutoCatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

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
        compraProdutoRepository.findByDemonstracaoTrueAndAtivaTrue().forEach(compra -> {
            if (!produtoCatalogoRepository.existsById(compra.getProdutoId())
                    || avaliacaoRepository.existsByCompraIdAndProdutoId(compra.getCompraId(), compra.getProdutoId())) {
                return;
            }
            int nota = (int) (compra.getProdutoId() % 2) + 4;
            avaliacaoRepository.save(Avaliacao.builder()
                    .produtoId(compra.getProdutoId())
                    .compraId(compra.getCompraId())
                    .usuarioId(compra.getUsuarioId())
                    .nomeUsuario(compra.getNomeUsuario() == null ? "Cliente" : compra.getNomeUsuario())
                    .nota(nota)
                    .comentario("Produto comprado e aprovado na demonstração.")
                    .build());
        });

        completarAvaliacoesDoCatalogo();
    }

    private void completarAvaliacoesDoCatalogo() {
        produtoCatalogoRepository.findAll().forEach(produto -> {
            List<Avaliacao> seedadas = new ArrayList<>(
                    avaliacaoRepository.findByProdutoIdAndCompraIdLessThanOrderByIdAsc(produto.getId(), 0L));
            long existentes = avaliacaoRepository.countByProdutoId(produto.getId());
            int alvo = 5 + Math.floorMod(produto.getId().intValue() * 7, 11);
            int totalAlvo = Math.max(alvo, Math.toIntExact(existentes));
            int quantidadeSeedadaAlvo = totalAlvo - Math.toIntExact(existentes - seedadas.size());
            double mediaDesejada = 2.4 + Math.floorMod(produto.getId().intValue() * 17, 25) / 10.0;

            while (seedadas.size() < quantidadeSeedadaAlvo) {
                int indice = seedadas.size();
                long compraDemoId = -(produto.getId() * 100_000L + indice + 1);
                String nome = NOMES_CLIENTES.get(Math.floorMod(produto.getId().intValue() + indice,
                        NOMES_CLIENTES.size()));
                seedadas.add(avaliacaoRepository.save(Avaliacao.builder()
                        .produtoId(produto.getId())
                        .compraId(compraDemoId)
                        .nomeUsuario(nome)
                        .nota(3)
                        .comentario(COMENTARIOS_POR_NOTA.get(2))
                        .build()));
            }

            for (int indice = 0; indice < seedadas.size(); indice++) {
                Avaliacao avaliacao = seedadas.get(indice);
                int nota = notaVariada(mediaDesejada, indice, seedadas.size());
                String nome = NOMES_CLIENTES.get(Math.floorMod(produto.getId().intValue() + indice,
                        NOMES_CLIENTES.size()));
                String comentario = COMENTARIOS_POR_NOTA.get(nota - 1);
                if (nota != avaliacao.getNota() || !comentario.equals(avaliacao.getComentario())
                        || !nome.equals(avaliacao.getNomeUsuario())) {
                    avaliacao.setNota(nota);
                    avaliacao.setComentario(comentario);
                    avaliacao.setNomeUsuario(nome);
                    avaliacaoRepository.save(avaliacao);
                }
            }
        });
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
