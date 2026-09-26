package com.pbtp1.config;

import com.pbtp1.auth.AuthService;
import com.pbtp1.model.*;
import com.pbtp1.repository.CategoriaRepository;
import com.pbtp1.repository.CompraRepository;
import com.pbtp1.repository.ProdutoRepository;
import com.pbtp1.service.CompraService;
import com.pbtp1.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.math.BigDecimal;

@Configuration
@ConditionalOnProperty(name = "app.demo.seed", havingValue = "true")
@RequiredArgsConstructor
public class DemoDataConfig {
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoService produtoService;
    private final AuthService authService;
    private final CompraService compraService;
    private final CompraRepository compraRepository;

    @Bean
    public CommandLineRunner demoDataSeeder() {
        return args -> {
            authService.criarInicial("Administrador", "admin@pbat.local", "admin123", PerfilUsuario.ADMIN);
            authService.criarInicial("Antonio Campbell", "user@pbat.local", "user123", PerfilUsuario.USER);

            Map<String, Categoria> categorias = new LinkedHashMap<>();
            categorias.put("Eletrônicos", categoria("Eletrônicos", "Tecnologia para o dia a dia"));
            categorias.put("Casa", categoria("Casa", "Conforto e utilidades"));
            categorias.put("Mobilidade", categoria("Mobilidade", "Produtos para ir mais longe"));
            categorias.put("Escritório", categoria("Escritório", "Produtividade e organização"));
            categorias.put("Áudio", categoria("Áudio", "Som para todos os momentos"));

            popularCatalogo(categorias);

            criarUsuariosEComprasDemonstracao();
        };
    }

    private void criarUsuariosEComprasDemonstracao() {
        var produtos = produtoRepository.findAll();
        String[] nomes = {"Ana Clara Martins", "Bruno Almeida", "Camila Souza", "Daniel Oliveira",
                "Eduarda Lima", "Felipe Santos", "Giovana Rocha", "Heitor Ribeiro",
                "Isabela Fernandes", "João Pedro Costa"};
        for (int indice = 1; indice <= nomes.length; indice++) {
            String email = "cliente%02d@pbat.local".formatted(indice);
            Usuario usuario = authService.criarInicial(nomes[indice - 1], email,
                    "cliente123", PerfilUsuario.USER);
            if (!compraRepository.findByUsuarioIdOrderByCriadaEmDesc(usuario.getId()).isEmpty()) {
                continue;
            }
            Produto produto = null;
            for (int tentativa = 0; tentativa < produtos.size(); tentativa++) {
                Produto candidato = produtos.get((indice - 1 + tentativa) % produtos.size());
                if (candidato.getStatus() != StatusProduto.INATIVO
                        && candidato.getStatus() != StatusProduto.ESGOTADO
                        && candidato.getEstoque() != null && candidato.getEstoque() > 0) {
                    produto = candidato;
                    break;
                }
            }
            if (produto == null) {
                continue;
            }
            compraService.criarDemonstracao(usuario.getId(), new CompraService.CompraRequest(
                    java.util.List.of(new CompraService.ItemRequest(produto.getId(), 1))));
        }
    }

    private Categoria categoria(String nome, String descricao) {
        return categoriaRepository.findByNomeIgnoreCase(nome)
                .orElseGet(() -> categoriaRepository.save(Categoria.builder().nome(nome).descricao(descricao).build()));
    }

    private void produto(String nome, String descricao, double preco, Categoria categoria, int estoque, StatusProduto status) {
        Produto existente = produtoRepository.findByNomeIgnoreCase(nome).orElse(null);
        Produto dados = Produto.builder().nome(nome).descricao(descricao).preco(BigDecimal.valueOf(preco)).categoria(categoria).estoque(estoque).status(status).build();
        if (existente == null) {
            produtoService.salvar(dados);
        } else if (existente.getStatus() == null || existente.getEstoque() == null || existente.getCategoria() == null) {
            produtoService.atualizar(existente.getId(), dados);
        }
    }

    private void popularCatalogo(Map<String, Categoria> categorias) {
        List<SeedProduto> seed = List.of(
                new SeedProduto("Notebook Ultra 14", "Notebook leve com 16GB de RAM e SSD de 512GB", 4299.90, "Escritório"),
                new SeedProduto("Fone Noise Cancelling", "Fone Bluetooth com cancelamento ativo de ruído", 799.90, "Áudio"),
                new SeedProduto("Camera Mirrorless X1", "Câmera mirrorless 24MP para fotografia e vídeo", 3599.00, "Eletrônicos"),
                new SeedProduto("Smartwatch Pulse", "Relógio inteligente com monitoramento de atividades", 649.50, "Eletrônicos"),
                new SeedProduto("Cafeteira Espresso Pro", "Cafeteira automática para espresso e cappuccino", 1199.00, "Casa"),
                new SeedProduto("Mochila Executiva", "Mochila resistente para notebook e acessórios", 289.90, "Escritório"),
                new SeedProduto("Tablet Horizon 11", "Tela ampla, bateria para o dia inteiro e capa inclusa", 1899.00, "Eletrônicos"),
                new SeedProduto("Console NovaPlay", "Console compacto com controle sem fio", 2499.00, "Eletrônicos"),
                new SeedProduto("Aspirador Orbital", "Aspirador vertical silencioso para a casa", 899.00, "Casa"),
                new SeedProduto("Bicicleta Urbana", "Bicicleta híbrida para deslocamentos urbanos", 2199.00, "Mobilidade"),
                new SeedProduto("Monitor Studio 27", "Monitor QHD com cores precisas para criação", 1799.00, "Escritório"),
                new SeedProduto("Teclado Mecânico Air", "Teclado compacto com switches silenciosos", 449.90, "Escritório"),
                new SeedProduto("Cadeira Rustica", "Cadeira confortável em madeira para sala de jantar", 389.90, "Casa"),
                new SeedProduto("Smartphone Aurora", "Smartphone 5G com câmera tripla e tela OLED", 2799.00, "Eletrônicos"),
                new SeedProduto("Televisor Vision 55", "Smart TV 4K de 55 polegadas com HDR", 3199.00, "Eletrônicos"),
                new SeedProduto("Impressora 3D Maker", "Impressora 3D compacta para projetos criativos", 1899.00, "Eletrônicos"),
                new SeedProduto("Roteador Mesh Connect", "Wi-Fi de alta velocidade para toda a casa", 699.90, "Eletrônicos"),
                new SeedProduto("Câmera de Segurança Smart", "Câmera Wi-Fi com visão noturna e detecção de movimento", 329.90, "Eletrônicos"),
                new SeedProduto("SSD Portátil Flash", "Armazenamento externo rápido e compacto de 1 TB", 599.90, "Eletrônicos"),
                new SeedProduto("Cafeteira Prensa Francesa", "Prensa de vidro para café encorpado", 129.90, "Casa"),
                new SeedProduto("Luminária de Mesa Nórdica", "Luminária articulada para leitura e trabalho", 219.90, "Casa"),
                new SeedProduto("Jogo de Panelas Ceramic", "Conjunto de panelas com revestimento cerâmico", 579.00, "Casa"),
                new SeedProduto("Purificador de Ar Breeze", "Purificador silencioso com filtro HEPA", 899.00, "Casa"),
                new SeedProduto("Liquidificador Vitamax", "Liquidificador potente com jarra de vidro", 349.90, "Casa"),
                new SeedProduto("Jogo de Cama Algodão", "Enxoval macio em algodão de toque suave", 299.90, "Casa"),
                new SeedProduto("Prateleira Modular", "Estante modular para organizar qualquer ambiente", 459.00, "Casa"),
                new SeedProduto("Patinete Elétrico Move", "Patinete dobrável com autonomia para o dia a dia", 2899.00, "Mobilidade"),
                new SeedProduto("Capacete Urbano Pro", "Capacete leve com ventilação reforçada", 249.90, "Mobilidade"),
                new SeedProduto("Patins Inline Street", "Patins ajustáveis para passeios e prática esportiva", 499.00, "Mobilidade"),
                new SeedProduto("Scooter City Ride", "Scooter urbana leve e econômica", 1799.00, "Mobilidade"),
                new SeedProduto("Mochila de Ciclismo", "Mochila compacta com reservatório de hidratação", 229.90, "Mobilidade"),
                new SeedProduto("Bicicleta Elétrica Volt", "Bicicleta elétrica para trajetos urbanos", 6499.00, "Mobilidade"),
                new SeedProduto("Bolsa de Guidão Explorer", "Bolsa impermeável para levar itens essenciais", 149.90, "Mobilidade"),
                new SeedProduto("Farol Recarregável Trail", "Farol dianteiro com bateria USB e múltiplos modos", 119.90, "Mobilidade"),
                new SeedProduto("Cadeado U-Lock Seguro", "Cadeado reforçado com cabo de aço", 189.90, "Mobilidade"),
                new SeedProduto("Caixa de Som Pulse", "Caixa Bluetooth portátil resistente à água", 399.90, "Áudio"),
                new SeedProduto("Headset Studio", "Headset confortável com microfone removível", 549.00, "Áudio"),
                new SeedProduto("Turntable Classic", "Toca-discos com pré-amplificador integrado", 1199.00, "Áudio"),
                new SeedProduto("Soundbar Cinema", "Soundbar com subwoofer sem fio para TV", 999.00, "Áudio"),
                new SeedProduto("Microfone Podcast USB", "Microfone condensador para gravações e chamadas", 429.90, "Áudio"),
                new SeedProduto("Fone Esportivo Run", "Fone sem fio com encaixe seguro para exercícios", 279.90, "Áudio"),
                new SeedProduto("Amplificador Hi-Fi", "Amplificador estéreo compacto para música", 1599.00, "Áudio"),
                new SeedProduto("Gravador Digital Voice", "Gravador portátil para entrevistas e aulas", 369.00, "Áudio"),
                new SeedProduto("DAC Headphone Pro", "Conversor de áudio de alta resolução", 689.00, "Áudio"),
                new SeedProduto("Mesa Elevatória Ergo", "Mesa com altura ajustável para escritório", 1599.00, "Escritório"),
                new SeedProduto("Webcam Full HD", "Webcam com foco automático para videoconferências", 299.90, "Escritório"),
                new SeedProduto("Mouse Vertical Comfort", "Mouse ergonômico sem fio para longas jornadas", 219.90, "Escritório"),
                new SeedProduto("Luminária de Monitor", "Barra de luz com brilho regulável para monitor", 189.90, "Escritório"),
                new SeedProduto("Suporte Notebook Alumínio", "Suporte ajustável para melhorar a ergonomia", 159.90, "Escritório"),
                new SeedProduto("Organizador de Cabos", "Kit de organização para mesa de trabalho", 69.90, "Escritório")
        );

        int[] estoques = {12, 18, 9, 14, 11, 20, 8, 16, 7, 13};
        int[] quantidadesPorCategoria = new int[categorias.size()];
        Map<String, Integer> indicesCategoria = new LinkedHashMap<>();
        int categoriaIndice = 0;
        for (String nome : categorias.keySet()) indicesCategoria.put(nome, categoriaIndice++);

        for (SeedProduto item : seed) {
            int indice = indicesCategoria.get(item.categoria());
            quantidadesPorCategoria[indice]++;
            produto(item.nome(), item.descricao(), item.preco(), categorias.get(item.categoria()),
                    estoques[quantidadesPorCategoria[indice] % estoques.length], StatusProduto.ATIVO);
        }
    }

    private record SeedProduto(String nome, String descricao, double preco, String categoria) {
    }
}
