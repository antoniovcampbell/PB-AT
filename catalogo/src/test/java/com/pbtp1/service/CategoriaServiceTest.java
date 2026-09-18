package com.pbtp1.service;

import com.pbtp1.model.Categoria;
import com.pbtp1.repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CategoriaServiceTest {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @BeforeEach
    void setUp() {
        categoriaRepository.deleteAll();
    }

    @Test
    void deveSalvarCategoria() {
        Categoria categoria = Categoria.builder()
                .nome("Informática")
                .descricao("Produtos de informática")
                .build();

        Categoria salva = categoriaService.salvar(categoria);

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getNome()).isEqualTo("Informática");
    }

    @Test
    void deveListarTodas() {
        categoriaService.salvar(Categoria.builder().nome("Cat1").build());
        categoriaService.salvar(Categoria.builder().nome("Cat2").build());

        List<Categoria> categorias = categoriaService.listarTodas();
        assertThat(categorias).hasSize(2);
    }

    @Test
    void deveBuscarPorNome() {
        categoriaService.salvar(Categoria.builder().nome("Única").build());

        Categoria encontrada = categoriaService.buscarPorNome("única");
        assertThat(encontrada).isNotNull();
    }

    @Test
    void deveLancarExcecaoAoBuscarNomeInexistente() {
        assertThatThrownBy(() -> categoriaService.buscarPorNome("inexistente"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveAtualizarCategoria() {
        Categoria salva = categoriaService.salvar(Categoria.builder().nome("Antiga").build());

        Categoria atualizada = categoriaService.atualizar(salva.getId(),
                Categoria.builder().nome("Nova").descricao("Atualizada").build());

        assertThat(atualizada.getNome()).isEqualTo("Nova");
        assertThat(atualizada.getDescricao()).isEqualTo("Atualizada");
    }

    @Test
    void deveDeletarCategoria() {
        Categoria salva = categoriaService.salvar(Categoria.builder().nome("Temp").build());
        categoriaService.deletar(salva.getId());

        assertThatThrownBy(() -> categoriaService.buscarPorId(salva.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoAoDeletarIdInexistente() {
        assertThatThrownBy(() -> categoriaService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
