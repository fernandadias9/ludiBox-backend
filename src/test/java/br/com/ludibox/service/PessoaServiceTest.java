package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.auth.RSAPasswordEncoder;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.PerfilDTO;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

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

    @Mock
    private MultipartFile multipartFile;

    private Pessoa pessoa;
    private Pessoa admin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        pessoa = new Pessoa();
        pessoa.setId(1);
        pessoa.setNome("João Silva");
        pessoa.setEmail("teste@email.com");
        pessoa.setSenha("senha123");
        pessoa.setTelefone("(11) 99999-9999");
        pessoa.setValorDocumento("12345678901");
        pessoa.setPerfil(EnumPerfil.USUARIO);
        pessoa.setSituacao(true);
        pessoa.setImagemUsuarioEmBase64("imagemBase64");

        admin = new Pessoa();
        admin.setId(2);
        admin.setNome("Admin User");
        admin.setEmail("admin@email.com");
        admin.setSenha("admin123");
        admin.setTelefone("(11) 88888-8888");
        admin.setValorDocumento("98765432100");
        admin.setPerfil(EnumPerfil.ADMINISTRADOR);
        admin.setSituacao(true);
    }

    // ========== TESTES PARA salvarImagemPessoa ==========

    @Test
    void testSalvarImagemPessoa_ComSucesso() throws LudiBoxException {
        String imagemBase64 = "imagemProcessadaBase64";

        when(pessoaRepository.findById(1)).thenReturn(Optional.of(pessoa));
        when(imagemService.processarImagem(multipartFile)).thenReturn(imagemBase64);
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        assertDoesNotThrow(() -> pessoaService.salvarImagemPessoa(multipartFile, 1));

        verify(imagemService).processarImagem(multipartFile);
        verify(pessoaRepository).save(argThat(p -> p.getImagemUsuarioEmBase64().equals(imagemBase64)));
    }

    @Test
    void testSalvarImagemPessoa_UsuarioNaoEncontrado_DeveLancarExcecao() {
        when(pessoaRepository.findById(1)).thenReturn(Optional.empty());

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvarImagemPessoa(multipartFile, 1));

        assertEquals("Erro: ", exception.getCampo());
        assertEquals("Usuario não encontrado", exception.getMensagem());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    // ========== TESTES PARA salvar ==========

    @Test
    void testSalvar_ComSucesso() {
        when(pessoaRepository.findByValorDocumentoAndSituacao(pessoa.getValorDocumento(), true))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByEmailAndSituacao(pessoa.getEmail(), true))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        Pessoa salva = pessoaService.salvar(pessoa);

        assertEquals(pessoa.getEmail(), salva.getEmail());
        assertEquals("11999999999", salva.getTelefone()); // Telefone formatado
        verify(pessoaRepository).save(any(Pessoa.class));
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    void testSalvar_DocumentoJaCadastrado_DeveLancarExcecao() {
        when(pessoaRepository.findByValorDocumentoAndSituacao(pessoa.getValorDocumento(), true))
                .thenReturn(Optional.of(pessoa));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Documento: ", exception.getCampo());
        assertEquals("Documento já cadastrado!", exception.getMensagem());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testSalvar_EmailJaCadastrado_DeveLancarExcecao() {
        when(pessoaRepository.findByValorDocumentoAndSituacao(pessoa.getValorDocumento(), true))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByEmailAndSituacao(pessoa.getEmail(), true))
                .thenReturn(Optional.of(pessoa));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Email: ", exception.getCampo());
        assertEquals("Email já cadastrado!", exception.getMensagem());
    }

    @Test
    void testSalvar_TelefoneVazio_DeveLancarExcecao() {
        pessoa.setTelefone("");

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Telefone: ", exception.getCampo());
        assertEquals("Número não pode estar vazio!", exception.getMensagem());
    }

    @Test
    void testSalvar_TelefoneNulo_DeveLancarExcecao() {
        pessoa.setTelefone(null);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Telefone: ", exception.getCampo());
        assertEquals("Número não pode estar vazio!", exception.getMensagem());
    }

    @Test
    void testSalvar_TelefoneComCaracteresInvalidos_DeveLancarExcecao() {
        pessoa.setTelefone("123abc");

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Telefone: ", exception.getCampo());
        assertEquals("Número contém caracteres inválidos!", exception.getMensagem());
    }

    @Test
    void testSalvar_TelefoneComTamanhoInvalido_DeveLancarExcecao() {
        pessoa.setTelefone("123456789"); // 9 dígitos

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Telefone: ", exception.getCampo());
        assertEquals("Número inserido é inválido!", exception.getMensagem());
    }

    @Test
    void testSalvar_TelefoneCom10Digitos_ComSucesso() {
        pessoa.setTelefone("(11) 9999-9999"); // 10 dígitos

        when(pessoaRepository.findByValorDocumentoAndSituacao(pessoa.getValorDocumento(), true))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByEmailAndSituacao(pessoa.getEmail(), true))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        Pessoa salva = pessoaService.salvar(pessoa);

        assertEquals("1199999999", salva.getTelefone());
    }

    @Test
    void testSalvar_EmailVazio_DeveLancarExcecao() {
        pessoa.setEmail("");

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Email: ", exception.getCampo());
        assertEquals("Email não pode estar vazio!", exception.getMensagem());
    }

    @Test
    void testSalvar_EmailNulo_DeveLancarExcecao() {
        pessoa.setEmail(null);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Email: ", exception.getCampo());
        assertEquals("Email não pode estar vazio!", exception.getMensagem());
    }

    @Test
    void testSalvar_EmailInvalido_DeveLancarExcecao() {
        pessoa.setEmail("invalido.com");

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Email: ", exception.getCampo());
        assertEquals("Email inserido é inválido!", exception.getMensagem());
    }

    @Test
    void testSalvar_EmailSemDominio_DeveLancarExcecao() {
        pessoa.setEmail("teste@");

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Email: ", exception.getCampo());
        assertEquals("Email inserido é inválido!", exception.getMensagem());
    }

    @Test
    void testCadastrarAdm_ComSucesso() {
        when(authService.getPessoaAutenticada()).thenReturn(admin);
        when(pessoaRepository.findByValorDocumentoAndSituacao(pessoa.getValorDocumento(), true))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByEmailAndSituacao(pessoa.getEmail(), true))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        Pessoa adminCadastrado = pessoaService.cadastrarAdm(pessoa);

        assertEquals(EnumPerfil.ADMINISTRADOR, adminCadastrado.getPerfil());
        verify(passwordEncoder).encode("senha123");
        verify(pessoaRepository).save(any(Pessoa.class));
    }

    @Test
    void testCadastrarAdm_UsuarioComum_DeveLancarExcecao() {
        when(authService.getPessoaAutenticada()).thenReturn(pessoa);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.cadastrarAdm(pessoa));

        assertEquals("Administração: ", exception.getCampo());
        assertEquals("Ação exclusiva para administradores!", exception.getMensagem());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void testCadastrarAdm_DocumentoJaExiste_DeveLancarExcecao() {
        when(authService.getPessoaAutenticada()).thenReturn(admin);
        when(pessoaRepository.findByValorDocumentoAndSituacao(pessoa.getValorDocumento(), true))
                .thenReturn(Optional.of(pessoa));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.cadastrarAdm(pessoa));

        assertEquals("Documento: ", exception.getCampo());
        assertEquals("Documento já cadastrado!", exception.getMensagem());
    }

    @Test
    void testBuscarTodos_ComSucesso() {
        List<Pessoa> pessoas = Arrays.asList(pessoa, admin);

        when(authService.getPessoaAutenticada()).thenReturn(admin);
        when(pessoaRepository.findAll()).thenReturn(pessoas);

        List<Pessoa> resultado = pessoaService.buscarTodos();

        assertEquals(2, resultado.size());
        assertEquals(pessoas, resultado);
    }

    @Test
    void testBuscarTodos_UsuarioComum_DeveLancarExcecao() {
        when(authService.getPessoaAutenticada()).thenReturn(pessoa);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.buscarTodos());

        assertEquals("Administração: ", exception.getCampo());
        assertEquals("Ação exclusiva para administradores!", exception.getMensagem());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void testAtualizarDados_ComSucesso() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("telefone", "(11)999999999");
        updates.put("email", "novo@email.com");

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        pessoa.setTelefone("(11)999999999");
        pessoa.setEmail("novo@email.com");

        Pessoa atualizada = pessoaService.atualizarDados(pessoa, updates);

        assertEquals("novo@email.com", atualizada.getEmail());
        assertEquals("11999999999", atualizada.getTelefone());
    }

    @Test
    void testAtualizarDados_CampoInvalido_DeveLancarExcecao() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("campoInexistente", "valor");

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.atualizarDados(pessoa, updates));

        assertEquals("Erro", exception.getCampo());
        assertTrue(exception.getMensagem().contains("Campo inválido ou não acessível"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testAtualizarDados_TentativaAlterarDocumento_DeveLancarExcecao() {
        pessoa.setValorDocumento("99999999999"); // Documento diferente
        Map<String, Object> updates = new HashMap<>();

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);

        Pessoa pessoaOriginal = new Pessoa();
        pessoaOriginal.setId(1);
        pessoaOriginal.setValorDocumento("12345678901");
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoaOriginal));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.atualizarDados(pessoa, updates));

        assertEquals("Documento: ", exception.getCampo());
        assertEquals("O documento não pode ser alterado!", exception.getMensagem());
    }

    @Test
    void testAtualizarDados_TentativaAlterarSituacao_DeveLancarExcecao() {
        pessoa.setSituacao(false);
        Map<String, Object> updates = new HashMap<>();

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.atualizarDados(pessoa, updates));

        assertEquals("Situação: ", exception.getCampo());
        assertEquals("A situação não pode ser alterada!", exception.getMensagem());
    }

    @Test
    void testAtualizarDados_TentativaAlterarPerfil_DeveLancarExcecao() {
        pessoa.setPerfil(EnumPerfil.ADMINISTRADOR);
        Map<String, Object> updates = new HashMap<>();

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.atualizarDados(pessoa, updates));

        assertEquals("Perfil: ", exception.getCampo());
        assertEquals("O tipo perfil não pode ser alterado!", exception.getMensagem());
    }

    @Test
    void testExcluirPessoa_ComSucesso() {
        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(pessoaRepository.findById(1)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        assertDoesNotThrow(() -> pessoaService.excluirPessoa(1));

        verify(pessoaRepository).save(argThat(p ->
                !p.isSituacao() && p.getDataDesativacao() != null));
    }

    @Test
    void testExcluirPessoa_PessoaNaoEncontrada_DeveLancarExcecao() {
        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(pessoaRepository.findById(1)).thenReturn(Optional.empty());

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.excluirPessoa(1));

        assertEquals("Pessoa não encontrada", exception.getCampo());
        assertEquals("Não foi possível encontrar uma pessoa com o ID: 1", exception.getMensagem());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testExcluirPessoa_UsuarioDiferente_DeveLancarExcecao() {
        Pessoa outraPessoa = new Pessoa();
        outraPessoa.setId(2);

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(pessoaRepository.findById(2)).thenReturn(Optional.of(outraPessoa));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.excluirPessoa(2));

        assertEquals("Acesso negado", exception.getCampo());
        assertEquals("Usuários só podem excluir seus próprios dados.", exception.getMensagem());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void testExcluirPessoa_PessoaJaExcluida_DeveLancarExcecao() {
        pessoa.setSituacao(false);

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(pessoaRepository.findById(1)).thenReturn(Optional.of(pessoa));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.excluirPessoa(1));

        assertEquals("Operação inválida", exception.getCampo());
        assertEquals("Essa pessoa já está excluída.", exception.getMensagem());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testReativarPessoa_ComSucesso() {
        pessoa.setSituacao(false);

        when(authService.getPessoaAutenticada()).thenReturn(admin);
        when(pessoaRepository.findById(1)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        assertDoesNotThrow(() -> pessoaService.reativarPessoa(1));

        verify(pessoaRepository).save(argThat(p -> p.isSituacao()));
    }

    @Test
    void testReativarPessoa_PessoaNaoEncontrada_DeveLancarExcecao() {
        when(authService.getPessoaAutenticada()).thenReturn(admin);
        when(pessoaRepository.findById(1)).thenReturn(Optional.empty());

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.reativarPessoa(1));

        assertEquals("ID: ", exception.getCampo());
        assertEquals("Pessoa não encontrada!", exception.getMensagem());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testBloquearPessoaFisica_ComSucesso() {
        when(authService.getPessoaAutenticada()).thenReturn(admin);
        when(pessoaRepository.findById(1)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        assertDoesNotThrow(() -> pessoaService.bloquearPessoaFisica(1));

        verify(pessoaRepository).save(argThat(p -> !p.isSituacao()));
    }

    @Test
    void testBloquearPessoaFisica_PessoaNaoEncontrada_DeveLancarExcecao() {
        when(authService.getPessoaAutenticada()).thenReturn(admin);
        when(pessoaRepository.findById(1)).thenReturn(Optional.empty());

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.bloquearPessoaFisica(1));

        assertEquals("ID: ", exception.getCampo());
        assertEquals("Pessoa não encontrada!", exception.getMensagem());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testBuscarPorId_ComSucesso() {
        when(pessoaRepository.findById(1)).thenReturn(Optional.of(pessoa));

        Pessoa resultado = pessoaService.buscarPorId(1);

        assertEquals(pessoa, resultado);
    }

    @Test
    void testBuscarPorId_PessoaNaoEncontrada_DeveLancarExcecao() {
        when(pessoaRepository.findById(1)).thenReturn(Optional.empty());

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.buscarPorId(1));

        assertEquals("ID: ", exception.getCampo());
        assertEquals("Usuário não encontrado!", exception.getMensagem());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testBuscarPerfilPorId_ComSucesso() {
        String senhaDecodificada = "senhaDecodificada";

        when(pessoaRepository.findById(1)).thenReturn(Optional.of(pessoa));
        when(rsaPasswordEncoder.decode(pessoa.getPassword())).thenReturn(senhaDecodificada);

        PerfilDTO perfil = pessoaService.buscarPerfilPorId(1);

        assertEquals(pessoa.getNome(), perfil.getNome());
        assertEquals(pessoa.getId(), perfil.getId());
        assertEquals(pessoa.getImagemUsuarioEmBase64(), perfil.getImagemUsuarioEmBase64());
        assertEquals(pessoa.getEmail(), perfil.getEmail());
        assertEquals(senhaDecodificada, perfil.getSenha());
        assertEquals(pessoa.getTelefone(), perfil.getTelefone());
        assertEquals(pessoa.getValorDocumento(), perfil.getValorDocumento());
    }

    @Test
    void testBuscarPerfilPorId_PessoaNaoEncontrada_DeveLancarExcecao() {
        when(pessoaRepository.findById(1)).thenReturn(Optional.empty());

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.buscarPerfilPorId(1));

        assertEquals("ID: ", exception.getCampo());
        assertEquals("Usuário não encontrado!", exception.getMensagem());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testAnonimizarDadosPessoa_ComSucesso() {
        when(pessoaRepository.findById(1)).thenReturn(Optional.of(pessoa));

        assertDoesNotThrow(() -> pessoaService.anonimizarDadosPessoa(1));

        verify(pessoaRepository).anonimizarDados(
                eq(1),
                eq("anonimizado_1@ludibox.com"),
                eq("00000000000")
        );
    }

    @Test
    void testAnonimizarDadosPessoa_PessoaNaoEncontrada() {
        when(pessoaRepository.findById(1)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> pessoaService.anonimizarDadosPessoa(1));

        verify(pessoaRepository, never()).anonimizarDados(anyInt(), anyString(), anyString());
    }

    @Test
    void testBuscarAdministradores_ComSucesso() {
        List<Pessoa> administradores = Arrays.asList(admin);

        when(pessoaRepository.findByPerfil(EnumPerfil.ADMINISTRADOR)).thenReturn(administradores);

        List<Pessoa> resultado = pessoaService.buscarAdministradores();

        assertEquals(1, resultado.size());
        assertEquals(admin, resultado.get(0));
    }

    @Test
    void testBuscarAdministradores_NenhumAdminEncontrado_DeveLancarExcecao() {
        when(pessoaRepository.findByPerfil(EnumPerfil.ADMINISTRADOR)).thenReturn(Collections.emptyList());

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.buscarAdministradores());

        assertEquals("Nenhum administrador encontrado", exception.getCampo());
        assertEquals("Sem administradores cadastrados", exception.getMensagem());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    void testValidarTelefone_ComEspacosECaracteresEspeciais() {
        pessoa.setTelefone("(11) 9 9999-9999");

        when(pessoaRepository.findByValorDocumentoAndSituacao(pessoa.getValorDocumento(), true))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByEmailAndSituacao(pessoa.getEmail(), true))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        Pessoa salva = pessoaService.salvar(pessoa);

        assertEquals("11999999999", salva.getTelefone());
    }

    @Test
    void testValidarEmail_ComDominioComplexo() {
        pessoa.setEmail("usuario.teste+tag@dominio.com.br");

        when(pessoaRepository.findByValorDocumentoAndSituacao(pessoa.getValorDocumento(), true))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByEmailAndSituacao(pessoa.getEmail(), true))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        Pessoa salva = pessoaService.salvar(pessoa);

        assertEquals("usuario.teste+tag@dominio.com.br", salva.getEmail());
    }

    @Test
    void testValidarEmail_ComDominioMuitoCurto_DeveLancarExcecao() {
        pessoa.setEmail("teste@a.b");

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Email: ", exception.getCampo());
        assertEquals("Email inserido é inválido!", exception.getMensagem());
    }

    @Test
    void testValidarEmail_SemArroba_DeveLancarExcecao() {
        pessoa.setEmail("testeemail.com");

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Email: ", exception.getCampo());
        assertEquals("Email inserido é inválido!", exception.getMensagem());
    }

    @Test
    void testValidarTelefone_Com12Digitos_DeveLancarExcecao() {
        pessoa.setTelefone("119999999999"); // 12 dígitos

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> pessoaService.salvar(pessoa));

        assertEquals("Telefone: ", exception.getCampo());
        assertEquals("Número inserido é inválido!", exception.getMensagem());
    }

    // ========== TESTES DE INTEGRAÇÃO ==========

    @Test
    void testFluxoCompleto_CadastroEAtualizacao() {
        // Primeiro, cadastrar a pessoa
        when(pessoaRepository.findByValorDocumentoAndSituacao(pessoa.getValorDocumento(), true))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByEmailAndSituacao(pessoa.getEmail(), true))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        Pessoa pessoaSalva = pessoaService.salvar(pessoa);
        assertNotNull(pessoaSalva);

        // Depois, atualizar os dados
        Map<String, Object> updates = new HashMap<>();
        updates.put("email", "novoemail@teste.com");

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));

        pessoa.setEmail("novoemail@teste.com");
        Pessoa pessoaAtualizada = pessoaService.atualizarDados(pessoa, updates);

        assertEquals("novoemail@teste.com", pessoaAtualizada.getEmail());
    }

    @Test
    void testFluxoCompleto_CadastroAdminEBusca() {
        // Cadastrar admin
        when(authService.getPessoaAutenticada()).thenReturn(admin);
        when(pessoaRepository.findByValorDocumentoAndSituacao(pessoa.getValorDocumento(), true))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByEmailAndSituacao(pessoa.getEmail(), true))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        Pessoa adminCadastrado = pessoaService.cadastrarAdm(pessoa);
        assertEquals(EnumPerfil.ADMINISTRADOR, adminCadastrado.getPerfil());

        // Buscar todos
        List<Pessoa> pessoas = Arrays.asList(pessoa, admin);
        when(pessoaRepository.findAll()).thenReturn(pessoas);

        List<Pessoa> resultado = pessoaService.buscarTodos();
        assertEquals(2, resultado.size());
    }
}