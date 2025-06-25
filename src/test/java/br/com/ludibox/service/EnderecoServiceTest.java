package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Endereco;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.repository.EnderecoRepository;
import br.com.ludibox.model.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnderecoServiceTest {

    @InjectMocks
    private EnderecoService enderecoService;

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private AuthenticationService authService;

    @Mock
    private CepService cepService;

    @Mock
    private PessoaRepository pessoaRepository;

    private Pessoa pessoa;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        pessoa = new Pessoa();
        pessoa.setId(1);
        pessoa.setEnderecos(new ArrayList<>());

        endereco = new Endereco();
        endereco.setId(10);
        endereco.setCep(12345678);
        endereco.setPessoa(pessoa);
    }

    @Test
    void testSalvarEnderecoParaPessoa_ComSucesso() {
        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(cepService.validarCep(endereco)).thenReturn(endereco);
        when(enderecoRepository.save(endereco)).thenReturn(endereco);

        Endereco salvo = enderecoService.salvarEnderecoParaPessoa(endereco);

        assertNotNull(salvo);
        assertEquals(pessoa, salvo.getPessoa());
        verify(enderecoRepository).save(endereco);
    }

    @Test
    void testAtualizarEndereco_ComSucesso() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("cidade", "Nova Cidade");

        endereco.setCidade("Antiga");

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        endereco.setPessoa(pessoa); // garantir associação

        when(enderecoRepository.save(endereco)).thenReturn(endereco);

        Endereco atualizado = enderecoService.atualizarEnderecoPessoa(endereco, updates);

        assertEquals("Nova Cidade", atualizado.getCidade());
        verify(enderecoRepository).save(endereco);
    }

    @Test
    void testAtualizarEndereco_OutroUsuarioLancaExcecao() {
        Pessoa outraPessoa = new Pessoa();
        outraPessoa.setId(2);
        endereco.setPessoa(outraPessoa);

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "SP");

        LudiBoxException ex = assertThrows(LudiBoxException.class, () ->
                enderecoService.atualizarEnderecoPessoa(endereco, updates));

        assertTrue(ex.getMessage().contains("Usuários só podem alterar seus próprios dados"));
    }

    @Test
    void testAtualizarEndereco_CampoInvalidoLancaExcecao() {
        endereco.setPessoa(pessoa);
        when(authService.getPessoaAutenticada()).thenReturn(pessoa);

        Map<String, Object> updates = new HashMap<>();
        updates.put("campoInexistente", "valor");

        LudiBoxException ex = assertThrows(LudiBoxException.class, () ->
                enderecoService.atualizarEnderecoPessoa(endereco, updates));

        assertTrue(ex.getMessage().contains("Campo inválido"));
    }

    @Test
    void testBuscarPorId_ComSucesso() {
        when(enderecoRepository.findById(10)).thenReturn(Optional.of(endereco));

        Endereco resultado = enderecoService.buscarPorId(10);
        assertEquals(endereco, resultado);
    }

    @Test
    void testBuscarPorId_NaoEncontradoLancaExcecao() {
        when(enderecoRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(LudiBoxException.class, () -> enderecoService.buscarPorId(10));
    }

    @Test
    void testListarEnderecosPorPessoa() {
        List<Endereco> lista = List.of(endereco);
        when(enderecoRepository.findByPessoaId(1)).thenReturn(lista);

        List<Endereco> resultado = enderecoService.listarEnderecosPorPessoa(1);

        assertEquals(1, resultado.size());
        assertEquals(endereco, resultado.get(0));
    }

    @Test
    void testDeletarEndereco_ComSucesso() {
        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(enderecoRepository.findById(10)).thenReturn(Optional.of(endereco));

        assertDoesNotThrow(() -> enderecoService.deletar(10));

        verify(enderecoRepository).delete(endereco);
    }

    @Test
    void testDeletarEndereco_DeOutroUsuarioLancaExcecao() {
        Pessoa outraPessoa = new Pessoa();
        outraPessoa.setId(2);
        endereco.setPessoa(outraPessoa);

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(enderecoRepository.findById(10)).thenReturn(Optional.of(endereco));

        LudiBoxException ex = assertThrows(LudiBoxException.class, () -> enderecoService.deletar(10));
        assertTrue(ex.getMessage().contains("Usuário não autorizado"));
    }

    @Test
    void testBuscarPorIdRuntimeException() {
        when(enderecoRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> enderecoService.buscarPorId(999));
    }
}
