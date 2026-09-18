package com.pbtp1.repository;

import com.pbtp1.model.Categoria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void deveSalvarEBuscarPorId() {
        Categoria categoria = Categoria.builder()
                .nome("Roupas")
                .descricao("Categoria de roupas")
                .build();

        Categoria salva = categoriaRepository.save(categoria);

        Optional<Categoria> encontrada = categoriaRepository.findById(salva.getId());
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNome()).isEqualTo("Roupas");
    }

    @Test
    void deveBuscarPorNomeIgnorandoCase() {
        categoriaRepository.save(Categoria.builder().nome("Alimentos").build());

        Optional<Categoria> encontrada = categoriaRepository.findByNomeIgnoreCase("alimentos");
        assertThat(encontrada).isPresent();
    }

    @Test
    void deveVerificarExistenciaPorNome() {
        categoriaRepository.save(Categoria.builder().nome("Bebidas").build());

        assertThat(categoriaRepository.existsByNomeIgnoreCase("bebidas")).isTrue();
        assertThat(categoriaRepository.existsByNomeIgnoreCase("inexistente")).isFalse();
    }

    @Test
    void deveAtualizarCategoria() {
        Categoria categoria = categoriaRepository.save(
                Categoria.builder().nome("Antiga").build()
        );

        categoria.setNome("Nova");
        categoria.setDescricao("Atualizada");
        categoriaRepository.save(categoria);

        Categoria atualizada = categoriaRepository.findById(categoria.getId()).orElseThrow();
        assertThat(atualizada.getNome()).isEqualTo("Nova");
        assertThat(atualizada.getDescricao()).isEqualTo("Atualizada");
    }

    @Test
    void deveDeletarCategoria() {
        Categoria categoria = categoriaRepository.save(
                Categoria.builder().nome("Temporária").build()
        );

        Long id = categoria.getId();
        categoriaRepository.deleteById(id);

        assertThat(categoriaRepository.findById(id)).isEmpty();
    }
}
