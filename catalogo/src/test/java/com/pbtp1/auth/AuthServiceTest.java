package com.pbtp1.auth;

import com.pbtp1.model.PerfilUsuario;
import com.pbtp1.model.Usuario;
import com.pbtp1.repository.UsuarioRepository;
import com.pbtp1.shared.auth.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final String SECRET = "test-secret-for-auth-service";

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setSecret() {
        ReflectionTestUtils.setField(authService, "secret", SECRET);
    }

    @Test
    void registrarCriaUsuarioNormalizadoEEmiteToken() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> {
            Usuario usuario = invocacao.getArgument(0);
            usuario.setId(7L);
            return usuario;
        });

        AuthService.AuthResponse resposta = authService.registrar(" Ana Silva ", " ANA@EXAMPLE.COM ", "senha123");

        assertThat(resposta.usuario().nome()).isEqualTo("Ana Silva");
        assertThat(resposta.usuario().email()).isEqualTo("ana@example.com");
        assertThat(resposta.usuario().perfil()).isEqualTo("USER");
        assertThat(TokenService.verify(resposta.token(), SECRET).email()).isEqualTo("ana@example.com");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getSenhaHash()).doesNotContain("senha123").contains(":");
    }

    @Test
    void registrarRejeitaDadosInvalidos() {
        assertThatThrownBy(() -> authService.registrar(" ", "ana@example.com", "senha123"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatThrownBy(() -> authService.registrar("Ana", "ana@example.com", "123"))
                .isInstanceOf(ResponseStatusException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrarRejeitaEmailDuplicado() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar("Ana", "ana@example.com", "senha123"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.CONFLICT);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void loginAceitaCredenciaisValidasEEmailComEspacos() {
        Usuario usuario = registrarEObterUsuario("ana@example.com", "senha123");
        when(usuarioRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(usuario));

        AuthService.AuthResponse resposta = authService.login(" ANA@example.com ", "senha123");

        assertThat(resposta.usuario().nome()).isEqualTo("Ana Silva");
        assertThat(TokenService.verify(resposta.token(), SECRET).id()).isEqualTo(42L);
    }

    @Test
    void loginRejeitaUsuarioInexistenteSenhaIncorretaEContaDesativada() {
        when(usuarioRepository.findByEmailIgnoreCase("ausente@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login("ausente@example.com", "senha123"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.UNAUTHORIZED);

        Usuario usuario = registrarEObterUsuario("ana@example.com", "senha123");
        when(usuarioRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(usuario));
        assertThatThrownBy(() -> authService.login("ana@example.com", "senha-errada"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.UNAUTHORIZED);

        usuario.setAtivo(false);
        assertThatThrownBy(() -> authService.login("ana@example.com", "senha123"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void requireUserValidaTokenExistenciaEAtivacao() {
        String token = TokenService.issue(42L, "ana@example.com", "Ana Silva", "USER", SECRET);
        Usuario usuario = usuario(42L, "ana@example.com", "Ana Silva", PerfilUsuario.USER, true);
        when(usuarioRepository.findById(42L)).thenReturn(Optional.of(usuario));

        assertThat(authService.requireUser("Bearer " + token)).isSameAs(usuario);
        assertThatThrownBy(() -> authService.requireUser("token-invalido"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.UNAUTHORIZED);

        when(usuarioRepository.findById(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.requireUser(token))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.UNAUTHORIZED);

        usuario.setAtivo(false);
        when(usuarioRepository.findById(42L)).thenReturn(Optional.of(usuario));
        assertThatThrownBy(() -> authService.requireUser(token))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void requireAdminPermiteAdminERejeitaUsuarioComum() {
        String adminToken = TokenService.issue(1L, "admin@example.com", "Admin", "ADMIN", SECRET);
        Usuario admin = usuario(1L, "admin@example.com", "Admin", PerfilUsuario.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        assertThat(authService.requireAdmin(adminToken)).isSameAs(admin);

        String userToken = TokenService.issue(2L, "user@example.com", "User", "USER", SECRET);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(
                usuario(2L, "user@example.com", "User", PerfilUsuario.USER, true)));
        assertThatThrownBy(() -> authService.requireAdmin(userToken))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void criarInicialInsereAtualizaNomeEPreservaNomeExistente() {
        when(usuarioRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        Usuario criado = authService.criarInicial("Ana", "ana@example.com", "senha123", PerfilUsuario.USER);
        assertThat(criado.getNome()).isEqualTo("Ana");

        Usuario existente = usuario(42L, "ana@example.com", "Nome antigo", PerfilUsuario.USER, true);
        when(usuarioRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(existente));
        Usuario atualizado = authService.criarInicial("Ana Silva", "ana@example.com", "nova-senha", PerfilUsuario.USER);
        assertThat(atualizado.getNome()).isEqualTo("Ana Silva");

        when(usuarioRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(atualizado));
        authService.criarInicial("Ana Silva", "ana@example.com", "outra-senha", PerfilUsuario.USER);
        verify(usuarioRepository).save(existente);
    }

    @Test
    void listaUsuariosEAtualizaAtivacaoComRegrasDeAdmin() {
        Usuario usuario = usuario(2L, "ana@example.com", "Ana", PerfilUsuario.USER, true);
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        assertThat(authService.listarUsuarios()).singleElement()
                .satisfies(dto -> assertThat(dto.email()).isEqualTo("ana@example.com"));

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        assertThat(authService.atualizarAtivo(2L, false).ativo()).isFalse();

        Usuario admin = usuario(1L, "admin@example.com", "Admin", PerfilUsuario.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        assertThatThrownBy(() -> authService.atualizarAtivo(1L, false))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.BAD_REQUEST);

        when(usuarioRepository.findById(3L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.atualizarAtivo(3L, true))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Usuario registrarEObterUsuario(String email, String senha) {
        when(usuarioRepository.existsByEmailIgnoreCase(email)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> {
            Usuario salvo = invocacao.getArgument(0);
            salvo.setId(42L);
            return salvo;
        });
        authService.registrar("Ana Silva", email, senha);
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        return captor.getValue();
    }

    private Usuario usuario(Long id, String email, String nome, PerfilUsuario perfil, boolean ativo) {
        return Usuario.builder().id(id).email(email).nome(nome).perfil(perfil).ativo(ativo)
                .senhaHash("hash-inutilizado").build();
    }
}
