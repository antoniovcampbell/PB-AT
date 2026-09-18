package com.pbtp1.controller;

import com.pbtp1.model.Produto;
import com.pbtp1.service.HistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/historico")
@RequiredArgsConstructor
public class HistoricoController {

    private final HistoricoService historicoService;

    @GetMapping("/produtos/{id}")
    public List<?> listarRevisoesProduto(@PathVariable Long id) {
        return historicoService.listarRevisoesProduto(id);
    }

    @GetMapping("/produtos")
    public List<?> listarTodasRevisoes() {
        return historicoService.listarTodasRevisoes();
    }

    @GetMapping("/produtos/{id}/revisao/{revision}")
    public Produto buscarProdutoNaRevisao(@PathVariable Long id,
                                          @PathVariable Integer revision) {
        return historicoService.buscarProdutoNaRevisao(id, revision);
    }
}
