package br.com.ludibox.service;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private PessoaRepository pessoaRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsernameUserFound() {
        Pessoa pessoa = new Pessoa();
        pessoa.setEmail("test@example.com");

        when(pessoaRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(pessoa));

        UserDetails result = authService.loadUserByUsername("test@example.com");

        assertEquals(pessoa, result);
    }

    @Test
    void testLoadUserByUsernameUserNotFound() {
        when(pessoaRepository.findByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> authService.loadUserByUsername("notfound@example.com"));
    }
}


