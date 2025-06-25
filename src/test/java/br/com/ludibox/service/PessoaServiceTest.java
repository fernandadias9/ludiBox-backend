package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.auth.RSAPasswordEncoder;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PessoaServiceTest {

    @InjectMocks
    private PessoaService pessoaService;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private AuthenticationService authService;

    @Mock
    private ImagemService imagemService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RSAPasswordEncoder rsaPasswordEncoder;

    private Pessoa pessoa;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pessoa = new Pessoa();
        pessoa.setId(1);
        pessoa.setEmail("teste@email.com");
        pessoa.setSenha("senha123");
        pessoa.setTelefone("(11) 99999-9999");
        pessoa.setValorDocumento("12345678901");
        pessoa.setPerfil(EnumPerfil.USUARIO);
        pessoa.setSituacao(true);
    }

    @Test
    void deveSalvarPessoaComSucesso() {
        when(pessoaRepository.findByValorDocumentoAndSituacao(pessoa.getValorDocumento(), true))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByEmailAndSituacao(pessoa.getEmail(), true))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        Pessoa salva = pessoaService.salvar(pessoa);

        assertEquals(pessoa.getEmail(), salva.getEmail());
        verify(pessoaRepository).save(any(Pessoa.class));
    }

    @Test
    void deveLancarExcecao_EmailJaCadastrado() {
        when(pessoaRepository.findByValorDocumentoAndSituacao(pessoa.getValorDocumento(), true))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByEmailAndSituacao(pessoa.getEmail(), true))
                .thenReturn(Optional.of(pessoa));

        LudiBoxException ex = assertThrows(LudiBoxException.class, () -> pessoaService.salvar(pessoa));
        assertEquals("Email: ", ex.getCampo());
        assertEquals("Email já cadastrado!", ex.getMensagem());
    }

    @Test
    void deveLancarExcecao_TelefoneInvalido() {
        pessoa.setTelefone("123abc");

        LudiBoxException ex = assertThrows(LudiBoxException.class, () -> pessoaService.salvar(pessoa));
        assertEquals("Telefone: ", ex.getCampo());
        assertEquals("Número contém caracteres inválidos!", ex.getMensagem());
    }

    @Test
    void deveLancarExcecao_EmailInvalido() {
        pessoa.setEmail("invalido.com");

        LudiBoxException ex = assertThrows(LudiBoxException.class, () -> pessoaService.salvar(pessoa));
        assertEquals("Email: ", ex.getCampo());
        assertEquals("Email inserido é inválido!", ex.getMensagem());
    }

    @Test
    void deveAtualizarDadosPessoa() {
        pessoa.setId(1);
        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        Map<String, Object> updates = new HashMap<>();
        updates.put("telefone", "(11)999999999");
        updates.put("email", "novo@email.com");

        pessoa.setTelefone("(11)999999999");
        pessoa.setEmail("novo@email.com");

        Pessoa atualizada = pessoaService.atualizarDados(pessoa, updates);

        assertEquals("novo@email.com", atualizada.getEmail());
    }

    @Test
    void deveExcluirPessoa() {
        pessoa.setId(1);
        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(pessoaRepository.findById(1)).thenReturn(Optional.of(pessoa));

        assertDoesNotThrow(() -> pessoaService.excluirPessoa(1));

        verify(pessoaRepository).save(argThat(p -> !p.isSituacao()));
    }

    @Test
    void deveBuscarAdministradores() {
        Pessoa admin = new Pessoa();
        admin.setId(2);
        admin.setPerfil(EnumPerfil.ADMINISTRADOR);
        when(pessoaRepository.findByPerfil(EnumPerfil.ADMINISTRADOR)).thenReturn(List.of(admin));

        List<Pessoa> admins = pessoaService.buscarAdministradores();

        assertEquals(1, admins.size());
    }

    @Test
    void deveLancarExcecao_AoBuscarAdminInexistente() {
        when(pessoaRepository.findByPerfil(EnumPerfil.ADMINISTRADOR)).thenReturn(Collections.emptyList());

        LudiBoxException ex = assertThrows(LudiBoxException.class, () -> pessoaService.buscarAdministradores());
        assertEquals("Nenhum administrador encontrado", ex.getCampo());
        assertEquals("Sem administradores cadastrados", ex.getMensagem());
    }
}
