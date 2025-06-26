package br.com.ludibox.service.IA;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidadorMeiosContatoIATest {

    @InjectMocks
    private ValidadorMeiosContatoIA validadorMeiosContatoIA;

    @Mock
    private ModeradorIAClient iaClient;

    private Produto produto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        produto = new Produto();
        produto.setId(1);
        produto.setNome("Produto Teste");
        produto.setDescricao("Descrição do produto teste");
    }

    @Test
    void testValidar_SemMeiosContato_ComSucesso() {
        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se há números de telefone, e-mails, links ou qualquer meio de contato. Retorne {\"violation\": true, \"message\": \"Meio de contato identificado\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), produto.getDescricao());

        doNothing().when(iaClient).enviarPromptEValidar(expectedPrompt, "Meio de Contato");

        assertDoesNotThrow(() -> validadorMeiosContatoIA.validar(produto));

        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Meio de Contato");
    }

    @Test
    void testValidar_ComMeiosContato_DeveLancarExcecao() {
        produto.setNome("Produto com telefone 11999999999");
        produto.setDescricao("Entre em contato pelo email teste@email.com");

        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se há números de telefone, e-mails, links ou qualquer meio de contato. Retorne {\"violation\": true, \"message\": \"Meio de contato identificado\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), produto.getDescricao());

        LudiBoxException exception = new LudiBoxException("Meio de Contato", "Meio de contato identificado", HttpStatus.UNPROCESSABLE_ENTITY);
        doThrow(exception).when(iaClient).enviarPromptEValidar(expectedPrompt, "Meio de Contato");

        LudiBoxException thrownException = assertThrows(LudiBoxException.class,
                () -> validadorMeiosContatoIA.validar(produto));

        assertEquals(exception, thrownException);
        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Meio de Contato");
    }

    @Test
    void testValidar_ComNomeNulo_DeveGerarPromptCorreto() {
        produto.setNome(null);
        produto.setDescricao("Descrição teste");

        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se há números de telefone, e-mails, links ou qualquer meio de contato. Retorne {\"violation\": true, \"message\": \"Meio de contato identificado\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                "null", produto.getDescricao());

        doNothing().when(iaClient).enviarPromptEValidar(expectedPrompt, "Meio de Contato");

        assertDoesNotThrow(() -> validadorMeiosContatoIA.validar(produto));

        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Meio de Contato");
    }

    @Test
    void testValidar_ComDescricaoNula_DeveGerarPromptCorreto() {
        produto.setNome("Produto Teste");
        produto.setDescricao(null);

        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se há números de telefone, e-mails, links ou qualquer meio de contato. Retorne {\"violation\": true, \"message\": \"Meio de contato identificado\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), "null");

        doNothing().when(iaClient).enviarPromptEValidar(expectedPrompt, "Meio de Contato");

        assertDoesNotThrow(() -> validadorMeiosContatoIA.validar(produto));

        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Meio de Contato");
    }

    @Test
    void testValidar_ComCaracteresEspeciais_DeveGerarPromptCorreto() {
        produto.setNome("Produto \"especial\" com 'aspas'");
        produto.setDescricao("Descrição com\nquebra de linha e\ttab");

        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se há números de telefone, e-mails, links ou qualquer meio de contato. Retorne {\"violation\": true, \"message\": \"Meio de contato identificado\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), produto.getDescricao());

        doNothing().when(iaClient).enviarPromptEValidar(expectedPrompt, "Meio de Contato");

        assertDoesNotThrow(() -> validadorMeiosContatoIA.validar(produto));

        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Meio de Contato");
    }

    @Test
    void testValidar_ErroInternoIA_DeveLancarExcecao() {
        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se há números de telefone, e-mails, links ou qualquer meio de contato. Retorne {\"violation\": true, \"message\": \"Meio de contato identificado\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), produto.getDescricao());

        LudiBoxException exception = new LudiBoxException("Meio de Contato", "Erro interno ao validar com IA", HttpStatus.INTERNAL_SERVER_ERROR);
        doThrow(exception).when(iaClient).enviarPromptEValidar(expectedPrompt, "Meio de Contato");

        LudiBoxException thrownException = assertThrows(LudiBoxException.class,
                () -> validadorMeiosContatoIA.validar(produto));

        assertEquals(exception, thrownException);
        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Meio de Contato");
    }

    @Test
    void testValidar_ComProdutoNulo_DeveLancarNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> validadorMeiosContatoIA.validar(null));

        verifyNoInteractions(iaClient);
    }
}