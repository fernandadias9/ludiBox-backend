package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.SenhasDTO;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResetarSenhaServiceTest {

    @InjectMocks
    private ResetarSenhaService resetarSenhaService;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder codificadorDeSenha;

    @Mock
    private AuthenticationService authService;

    private Pessoa pessoa;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pessoa = new Pessoa();
        pessoa.setEmail("teste@email.com");
        pessoa.setSenha("senhaCodificada");
    }

    @Test
    void resetarSenha_DeveGerarNovaSenhaEEnviarEmail() {
        when(pessoaRepository.findByEmail("teste@email.com")).thenReturn(Optional.of(pessoa));
        when(codificadorDeSenha.encode(anyString())).thenReturn("novaSenhaCodificada");

        assertDoesNotThrow(() -> resetarSenhaService.resetarSenha("teste@email.com"));

        verify(pessoaRepository).save(any(Pessoa.class));
        verify(emailService).enviarNovaSenha(eq("teste@email.com"), anyString());
    }

    @Test
    void alterarSenha_DadosInvalidos_DeveLancarExcecao() {
        SenhasDTO dto = new SenhasDTO();
        dto.senhaAtual = "123"; // muito curta
        dto.novaSenha = "123";
        dto.confirmarSenha = "123";

        LudiBoxException ex = assertThrows(LudiBoxException.class, () -> resetarSenhaService.alterarSenha(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus()); // CORRIGIDO
        assertEquals("Erro: ", ex.getCampo());
        assertEquals("Valores não inseridos ou senhas muito curtas!", ex.getMensagem());
    }

    @Test
    void alterarSenha_SenhaAtualIgualNova_DeveLancarExcecao() {
        SenhasDTO dto = new SenhasDTO();
        dto.senhaAtual = "senha123";
        dto.novaSenha = "senha123";
        dto.confirmarSenha = "senha123";

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);

        LudiBoxException ex = assertThrows(LudiBoxException.class, () -> resetarSenhaService.alterarSenha(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus()); // CORRIGIDO
        assertEquals("Erro:", ex.getCampo());
        assertEquals("Senha atual inválida", ex.getMensagem());
    }

    @Test
    void alterarSenha_SenhasNaoConferem_DeveLancarExcecao() {
        SenhasDTO dto = new SenhasDTO();
        dto.senhaAtual = "senha123";
        dto.novaSenha = "nova123";
        dto.confirmarSenha = "diferente";

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);

        LudiBoxException ex = assertThrows(LudiBoxException.class, () -> resetarSenhaService.alterarSenha(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus()); // CORRIGIDO
        assertEquals("Erro: ", ex.getCampo());
        assertEquals("Nova senha e confirmação não coincidem", ex.getMensagem());
    }

    @Test
    void alterarSenha_SenhaAtualIncorreta_DeveLancarExcecao() {
        SenhasDTO dto = new SenhasDTO();
        dto.senhaAtual = "senhaErrada";
        dto.novaSenha = "nova123";
        dto.confirmarSenha = "nova123";

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(codificadorDeSenha.matches("senhaErrada", "senhaCodificada")).thenReturn(false);

        LudiBoxException ex = assertThrows(LudiBoxException.class, () -> resetarSenhaService.alterarSenha(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus()); // CORRIGIDO
        assertEquals("Erro:", ex.getCampo());
        assertEquals("Senha atual inválida", ex.getMensagem());
    }


    @Test
    void alterarSenha_Sucesso_DeveAtualizarSenha() throws LudiBoxException {
        SenhasDTO dto = new SenhasDTO();
        dto.senhaAtual = "senhaAtual";
        dto.novaSenha = "novaSenha";
        dto.confirmarSenha = "novaSenha";

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(codificadorDeSenha.matches("senhaAtual", "senhaCodificada")).thenReturn(true);
        when(codificadorDeSenha.encode("novaSenha")).thenReturn("novaSenhaCodificada");

        assertDoesNotThrow(() -> resetarSenhaService.alterarSenha(dto));
        verify(pessoaRepository).save(pessoa);
    }
}
