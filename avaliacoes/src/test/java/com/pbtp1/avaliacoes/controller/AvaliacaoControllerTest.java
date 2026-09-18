package com.pbtp1.avaliacoes.controller;

import com.pbtp1.avaliacoes.service.AvaliacaoService;
import com.pbtp1.shared.dto.AvaliacaoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AvaliacaoController.class)
class AvaliacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvaliacaoService avaliacaoService;

    private final AvaliacaoDTO avaliacaoDTO = new AvaliacaoDTO(
            1L, 10L, "Ana", 5, "Ótimo", LocalDateTime.now());

    @Test
    void deveListarAvaliacoesPorProduto() throws Exception {
        when(avaliacaoService.listarPorProduto(10L))
                .thenReturn(List.of(avaliacaoDTO));

        mockMvc.perform(get("/avaliacoes/produtos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeUsuario").value("Ana"))
                .andExpect(jsonPath("$[0].nota").value(5));
    }

    @Test
    void deveListarTodasAvaliacoes() throws Exception {
        when(avaliacaoService.listarTodas()).thenReturn(List.of(avaliacaoDTO));

        mockMvc.perform(get("/avaliacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deveBuscarAvaliacaoPorId() throws Exception {
        when(avaliacaoService.buscarPorId(1L)).thenReturn(avaliacaoDTO);

        mockMvc.perform(get("/avaliacoes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(10))
                .andExpect(jsonPath("$.nomeUsuario").value("Ana"));
    }

    @Test
    void deveCalcularMedia() throws Exception {
        when(avaliacaoService.calcularMediaPorProduto(10L))
                .thenReturn(new AvaliacaoService.MediaAvaliacao(10L, 4.5, 2L));

        mockMvc.perform(get("/avaliacoes/produtos/10/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.media").value(4.5))
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void deveCriarAvaliacaoValida() throws Exception {
        when(avaliacaoService.salvar(org.mockito.ArgumentMatchers.any()))
                .thenReturn(avaliacaoDTO);

        mockMvc.perform(post("/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": 10,
                                  "nomeUsuario": "Ana",
                                  "nota": 5,
                                  "comentario": "Ótimo"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nota").value(5));
    }

    @Test
    void deveRejeitarAvaliacaoComNotaInvalida() throws Exception {
        mockMvc.perform(post("/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": 10,
                                  "nomeUsuario": "Ana",
                                  "nota": 6
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAtualizarAvaliacao() throws Exception {
        when(avaliacaoService.atualizar(anyLong(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(avaliacaoDTO);

        mockMvc.perform(put("/avaliacoes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": 10,
                                  "nomeUsuario": "Ana",
                                  "nota": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveDeletarAvaliacao() throws Exception {
        mockMvc.perform(delete("/avaliacoes/1"))
                .andExpect(status().isNoContent());
    }
}