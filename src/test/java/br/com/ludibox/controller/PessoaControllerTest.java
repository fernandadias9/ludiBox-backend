    package br.com.ludibox.controller;

    import br.com.ludibox.auth.AuthenticationService;
    import br.com.ludibox.exception.LudiBoxException;
    import br.com.ludibox.model.dto.PerfilDTO;
    import br.com.ludibox.model.entity.Pessoa;
    import br.com.ludibox.model.enums.EnumPerfil;
    import br.com.ludibox.service.PessoaService;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.MockitoAnnotations;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
    import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.boot.test.mock.mockito.MockBean;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.mock.web.MockMultipartFile;
    import org.springframework.test.web.servlet.MockMvc;
    import org.springframework.test.web.servlet.setup.MockMvcBuilders;
    import org.springframework.validation.BindingResult;
    import org.springframework.web.multipart.MultipartFile;

    import java.util.*;

    import static org.junit.jupiter.api.Assertions.*;
    import static org.mockito.Mockito.*;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    @SpringBootTest
    @AutoConfigureMockMvc
    class PessoaControllerTest {

        @InjectMocks
        private PessoaController pessoaController;

        @MockBean
        private PessoaService pessoaService;

        @MockBean
        private AuthenticationService authService;

        @Mock
        private BindingResult bindingResult;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private Pessoa pessoa;
        private Pessoa admin;
        private PerfilDTO perfilDTO;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            mockMvc = MockMvcBuilders.standaloneSetup(pessoaController).build();
            objectMapper = new ObjectMapper();

            pessoa = new Pessoa();
            pessoa.setId(1);
            pessoa.setNome("João Silva");
            pessoa.setEmail("joao@email.com");
            pessoa.setPerfil(EnumPerfil.USUARIO);
            pessoa.setSituacao(true);

            admin = new Pessoa();
            admin.setId(2);
            admin.setNome("Admin User");
            admin.setEmail("admin@email.com");
            admin.setPerfil(EnumPerfil.ADMINISTRADOR);
            admin.setSituacao(true);

            perfilDTO = new PerfilDTO();
            perfilDTO.setId(1);
            perfilDTO.setNome("João Silva");
            perfilDTO.setEmail("joao@email.com");
        }

        // ========== TESTES PARA uploadPessoa ==========

        @Test
        void testUploadPessoa_ComSucesso() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "imagem",
                    "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );
            Integer idPessoa = 1;

            when(authService.getPessoaAutenticada()).thenReturn(pessoa);
            doNothing().when(pessoaService).salvarImagemPessoa(file, idPessoa);

            ResponseEntity<String> response = pessoaController.uploadPessoa(file, idPessoa);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Imagem atualizada com sucesso", response.getBody());
            verify(authService).getPessoaAutenticada();
            verify(pessoaService).salvarImagemPessoa(file, idPessoa);
        }

        @Test
        void testUploadPessoa_ComArquivoNulo_DeveLancarExcecao() {
            Integer idPessoa = 1;

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.uploadPessoa(null, idPessoa));

            assertEquals("Erro: ", exception.getCampo());
            assertEquals("Arquivo inválido", exception.getMensagem());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            verifyNoInteractions(authService);
            verifyNoInteractions(pessoaService);
        }

        @Test
        void testUploadPessoa_ComArquivoVazio_DeveLancarExcecao() {
            MockMultipartFile file = new MockMultipartFile(
                    "imagem",
                    "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    new byte[0]
            );
            Integer idPessoa = 1;

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.uploadPessoa(file, idPessoa));

            assertEquals("Erro: ", exception.getCampo());
            assertEquals("Arquivo inválido", exception.getMensagem());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            verifyNoInteractions(authService);
            verifyNoInteractions(pessoaService);
        }

        @Test
        void testUploadPessoa_ComUsuarioNaoAutenticado_DeveLancarExcecao() {
            MockMultipartFile file = new MockMultipartFile(
                    "imagem",
                    "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );
            Integer idPessoa = 1;

            when(authService.getPessoaAutenticada()).thenReturn(null);

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.uploadPessoa(file, idPessoa));

            assertEquals("Não autorizado: ", exception.getCampo());
            assertEquals("Usuário sem permissão de acesso", exception.getMensagem());
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
            verify(authService).getPessoaAutenticada();
            verifyNoInteractions(pessoaService);
        }

        @Test
        void testUploadPessoa_ComErroNoService_DeveLancarExcecao() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "imagem",
                    "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );
            Integer idPessoa = 1;

            when(authService.getPessoaAutenticada()).thenReturn(pessoa);
            doThrow(new LudiBoxException("Erro", "Erro ao salvar imagem", HttpStatus.INTERNAL_SERVER_ERROR))
                    .when(pessoaService).salvarImagemPessoa(file, idPessoa);

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.uploadPessoa(file, idPessoa));

            assertEquals("Erro", exception.getCampo());
            assertEquals("Erro ao salvar imagem", exception.getMensagem());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        }

        // ========== TESTES PARA cadastrarAdm ==========

        @Test
        void testCadastrarAdm_ComSucesso() throws Exception {
            when(pessoaService.cadastrarAdm(pessoa)).thenReturn(admin);

            ResponseEntity<Pessoa> response = pessoaController.cadastrarAdm(pessoa);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(admin, response.getBody());
            verify(pessoaService).cadastrarAdm(pessoa);
        }

        @Test
        void testCadastrarAdm_ComErroNoService_DeveLancarExcecao() throws Exception {
            when(pessoaService.cadastrarAdm(pessoa))
                    .thenThrow(new LudiBoxException("Erro", "Erro ao cadastrar admin", HttpStatus.BAD_REQUEST));

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.cadastrarAdm(pessoa));

            assertEquals("Erro", exception.getCampo());
            assertEquals("Erro ao cadastrar admin", exception.getMensagem());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        }

        @Test
        void testCadastrarAdm_ComPessoaNula_DeveLancarExcecao() throws Exception {
            when(pessoaService.cadastrarAdm(null))
                    .thenThrow(new LudiBoxException("Erro", "Dados inválidos", HttpStatus.BAD_REQUEST));

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.cadastrarAdm(null));

            assertEquals("Erro", exception.getCampo());
            assertEquals("Dados inválidos", exception.getMensagem());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        }


        @Test
        void testAtualizarPessoa_ComPessoaNaoEncontrada_DeveRetornarNotFound() throws Exception {
            int id = 999;
            Map<String, Object> pessoaDetails = new HashMap<>();

            when(pessoaService.buscarPorId(id)).thenReturn(null);

            ResponseEntity<Pessoa> response = pessoaController.atualizarPessoa(id, pessoaDetails, bindingResult);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
            verify(pessoaService).buscarPorId(id);
            verifyNoMoreInteractions(pessoaService);
        }

        @Test
        void testAtualizarPessoa_ComErroNoService_DeveLancarExcecao() throws Exception {
            int id = 1;
            Map<String, Object> pessoaDetails = new HashMap<>();

            when(pessoaService.buscarPorId(id)).thenReturn(pessoa);
            when(bindingResult.hasErrors()).thenReturn(false);
            doThrow(new LudiBoxException("Erro", "Erro ao atualizar", HttpStatus.INTERNAL_SERVER_ERROR))
                    .when(pessoaService).atualizarDados(pessoa, pessoaDetails);

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.atualizarPessoa(id, pessoaDetails, bindingResult));

            assertEquals("Erro", exception.getCampo());
            assertEquals("Erro ao atualizar", exception.getMensagem());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        }

        @Test
        void testDesativarPessoa_ComSucesso() throws Exception {
            int id = 1;

            doNothing().when(pessoaService).excluirPessoa(id);

            assertDoesNotThrow(() -> pessoaController.desativarPessoa(id));

            verify(pessoaService).excluirPessoa(id);
        }

        @Test
        void testDesativarPessoa_ComErroNoService_DeveLancarExcecao() throws Exception {
            int id = 1;

            doThrow(new LudiBoxException("Erro", "Erro ao desativar pessoa", HttpStatus.BAD_REQUEST))
                    .when(pessoaService).excluirPessoa(id);

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.desativarPessoa(id));

            assertEquals("Erro", exception.getCampo());
            assertEquals("Erro ao desativar pessoa", exception.getMensagem());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        }

        @Test
        void testDesativarPessoa_ComIdInvalido_DeveLancarExcecao() throws Exception {
            int id = -1;

            doThrow(new LudiBoxException("Erro", "ID inválido", HttpStatus.BAD_REQUEST))
                    .when(pessoaService).excluirPessoa(id);

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.desativarPessoa(id));

            assertEquals("Erro", exception.getCampo());
            assertEquals("ID inválido", exception.getMensagem());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        }

        // ========== TESTES PARA reativarPessoa ==========

        @Test
        void testReativarPessoa_ComSucesso() throws Exception {
            int id = 1;

            doNothing().when(pessoaService).reativarPessoa(id);

            assertDoesNotThrow(() -> pessoaController.reativarPessoa(id));

            verify(pessoaService).reativarPessoa(id);
        }

        @Test
        void testReativarPessoa_ComErroNoService_DeveLancarExcecao() throws Exception {
            int id = 1;

            doThrow(new LudiBoxException("Erro", "Erro ao reativar pessoa", HttpStatus.BAD_REQUEST))
                    .when(pessoaService).reativarPessoa(id);

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.reativarPessoa(id));

            assertEquals("Erro", exception.getCampo());
            assertEquals("Erro ao reativar pessoa", exception.getMensagem());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        }

        @Test
        void testReativarPessoa_ComIdInvalido_DeveLancarExcecao() throws Exception {
            int id = 0;

            doThrow(new LudiBoxException("Erro", "ID inválido", HttpStatus.BAD_REQUEST))
                    .when(pessoaService).reativarPessoa(id);

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.reativarPessoa(id));

            assertEquals("Erro", exception.getCampo());
            assertEquals("ID inválido", exception.getMensagem());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        }

        // ========== TESTES PARA bloquearPessoa ==========

        @Test
        void testBloquearPessoa_ComSucesso() throws Exception {
            int id = 1;

            doNothing().when(pessoaService).bloquearPessoaFisica(id);

            assertDoesNotThrow(() -> pessoaController.bloquearPessoa(id));

            verify(pessoaService).bloquearPessoaFisica(id);
        }

        @Test
        void testBloquearPessoa_ComErroNoService_DeveLancarExcecao() throws Exception {
            int id = 1;

            doThrow(new LudiBoxException("Erro", "Erro ao bloquear pessoa", HttpStatus.BAD_REQUEST))
                    .when(pessoaService).bloquearPessoaFisica(id);

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.bloquearPessoa(id));

            assertEquals("Erro", exception.getCampo());
            assertEquals("Erro ao bloquear pessoa", exception.getMensagem());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        }

        @Test
        void testBloquearPessoa_ComIdNegativo_DeveLancarExcecao() throws Exception {
            int id = -5;

            doThrow(new LudiBoxException("Erro", "ID inválido", HttpStatus.BAD_REQUEST))
                    .when(pessoaService).bloquearPessoaFisica(id);

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.bloquearPessoa(id));

            assertEquals("Erro", exception.getCampo());
            assertEquals("ID inválido", exception.getMensagem());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        }

        // ========== TESTES PARA buscarTodasPessoas ==========

        @Test
        void testBuscarTodasPessoas_ComSucesso() throws Exception {
            List<Pessoa> pessoas = Arrays.asList(pessoa, admin);

            when(pessoaService.buscarTodos()).thenReturn(pessoas);

            List<Pessoa> result = pessoaController.buscarTodasPessoas();

            assertEquals(2, result.size());
            assertEquals(pessoas, result);
            verify(pessoaService).buscarTodos();
        }

        @Test
        void testBuscarTodasPessoas_ComListaVazia_DeveRetornarListaVazia() throws Exception {
            List<Pessoa> pessoas = new ArrayList<>();

            when(pessoaService.buscarTodos()).thenReturn(pessoas);

            List<Pessoa> result = pessoaController.buscarTodasPessoas();

            assertEquals(0, result.size());
            assertTrue(result.isEmpty());
            verify(pessoaService).buscarTodos();
        }

        @Test
        void testBuscarTodasPessoas_ComErroNoService_DeveLancarExcecao() throws Exception {
            when(pessoaService.buscarTodos())
                    .thenThrow(new LudiBoxException("Erro", "Erro ao buscar pessoas", HttpStatus.INTERNAL_SERVER_ERROR));

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.buscarTodasPessoas());

            assertEquals("Erro", exception.getCampo());
            assertEquals("Erro ao buscar pessoas", exception.getMensagem());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        }

        // ========== TESTES PARA buscarAdministradores ==========

        @Test
        void testBuscarAdministradores_ComSucesso() throws Exception {
            List<Pessoa> administradores = Arrays.asList(admin);

            when(pessoaService.buscarAdministradores()).thenReturn(administradores);

            List<Pessoa> result = pessoaController.buscarAdministradores();

            assertEquals(1, result.size());
            assertEquals(administradores, result);
            assertEquals(EnumPerfil.ADMINISTRADOR, result.get(0).getPerfil());
            verify(pessoaService).buscarAdministradores();
        }

        @Test
        void testBuscarAdministradores_ComListaVazia_DeveRetornarListaVazia() throws Exception {
            List<Pessoa> administradores = new ArrayList<>();

            when(pessoaService.buscarAdministradores()).thenReturn(administradores);

            List<Pessoa> result = pessoaController.buscarAdministradores();

            assertEquals(0, result.size());
            assertTrue(result.isEmpty());
            verify(pessoaService).buscarAdministradores();
        }

        @Test
        void testBuscarAdministradores_ComErroNoService_DeveLancarExcecao() throws Exception {
            when(pessoaService.buscarAdministradores())
                    .thenThrow(new LudiBoxException("Erro", "Erro ao buscar administradores", HttpStatus.INTERNAL_SERVER_ERROR));

            LudiBoxException exception = assertThrows(LudiBoxException.class,
                    () -> pessoaController.buscarAdministradores());

            assertEquals("Erro", exception.getCampo());
            assertEquals("Erro ao buscar administradores", exception.getMensagem());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        }

        // ========== TESTES PARA buscarPerfilPorId ==========

        @Test
        void testBuscarPerfilPorId_ComSucesso() {
            int id = 1;

            when(pessoaService.buscarPerfilPorId(id)).thenReturn(perfilDTO);

            PerfilDTO result = pessoaController.buscarPerfilPorId(id);

            assertEquals(perfilDTO, result);
            assertEquals(id, result.getId());
            assertEquals("João Silva", result.getNome());
            assertEquals("joao@email.com", result.getEmail());
            verify(pessoaService).buscarPerfilPorId(id);
        }

        @Test
        void testBuscarPerfilPorId_ComIdInexistente_DeveRetornarNull() {
            int id = 999;

            when(pessoaService.buscarPerfilPorId(id)).thenReturn(null);

            PerfilDTO result = pessoaController.buscarPerfilPorId(id);

            assertNull(result);
            verify(pessoaService).buscarPerfilPorId(id);
        }

        @Test
        void testBuscarPerfilPorId_ComIdNegativo_DevePassarParaService() {
            int id = -1;

            when(pessoaService.buscarPerfilPorId(id)).thenReturn(null);

            PerfilDTO result = pessoaController.buscarPerfilPorId(id);

            assertNull(result);
            verify(pessoaService).buscarPerfilPorId(id);
        }

        @Test
        void testBuscarPerfilPorId_ComIdZero_DevePassarParaService() {
            int id = 0;

            when(pessoaService.buscarPerfilPorId(id)).thenReturn(null);

            PerfilDTO result = pessoaController.buscarPerfilPorId(id);

            assertNull(result);
            verify(pessoaService).buscarPerfilPorId(id);
        }

        // ========== TESTES DE INTEGRAÇÃO E CENÁRIOS COMPLEXOS ==========

        @Test
        void testUploadPessoa_ComDiferentesTiposArquivo_ComSucesso() throws Exception {
            // Teste com PNG
            MockMultipartFile pngFile = new MockMultipartFile(
                    "imagem",
                    "test.png",
                    MediaType.IMAGE_PNG_VALUE,
                    "png image content".getBytes()
            );

            when(authService.getPessoaAutenticada()).thenReturn(pessoa);
            doNothing().when(pessoaService).salvarImagemPessoa(pngFile, 1);

            ResponseEntity<String> response = pessoaController.uploadPessoa(pngFile, 1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Imagem atualizada com sucesso", response.getBody());

            // Teste com GIF
            MockMultipartFile gifFile = new MockMultipartFile(
                    "imagem",
                    "test.gif",
                    MediaType.IMAGE_GIF_VALUE,
                    "gif image content".getBytes()
            );

            doNothing().when(pessoaService).salvarImagemPessoa(gifFile, 1);

            response = pessoaController.uploadPessoa(gifFile, 1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Imagem atualizada com sucesso", response.getBody());
        }

        @Test
        void testUploadPessoa_ComArquivoMuitoGrande_DevePassarParaService() throws Exception {
            byte[] largeContent = new byte[10 * 1024 * 1024]; // 10MB
            Arrays.fill(largeContent, (byte) 1);

            MockMultipartFile largeFile = new MockMultipartFile(
                    "imagem",
                    "large.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    largeContent
            );

            when(authService.getPessoaAutenticada()).thenReturn(pessoa);
            doNothing().when(pessoaService).salvarImagemPessoa(largeFile, 1);

            ResponseEntity<String> response = pessoaController.uploadPessoa(largeFile, 1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(pessoaService).salvarImagemPessoa(largeFile, 1);
        }



        @Test
        void testBuscarTodasPessoas_ComMuitasPessoas_ComSucesso() throws Exception {
            List<Pessoa> muitasPessoas = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                Pessoa p = new Pessoa();
                p.setId(i);
                p.setNome("Pessoa " + i);
                p.setEmail("pessoa" + i + "@email.com");
                muitasPessoas.add(p);
            }

            when(pessoaService.buscarTodos()).thenReturn(muitasPessoas);

            List<Pessoa> result = pessoaController.buscarTodasPessoas();

            assertEquals(1000, result.size());
            assertEquals(muitasPessoas, result);
        }

        // ========== TESTES DE VALIDAÇÃO DE PARÂMETROS ==========

        @Test
        void testUploadPessoa_ComIdPessoaNulo_DevePassarParaService() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "imagem",
                    "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test content".getBytes()
            );

            when(authService.getPessoaAutenticada()).thenReturn(pessoa);
            doNothing().when(pessoaService).salvarImagemPessoa(file, null);

            ResponseEntity<String> response = pessoaController.uploadPessoa(file, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(pessoaService).salvarImagemPessoa(file, null);
        }


    }