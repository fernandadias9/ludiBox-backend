package br.com.ludibox.service;

import br.com.ludibox.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return pessoaRepository.findByEmail(username)
                .map(user -> (UserDetails) user)
                .orElseGet(() -> pessoaRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("usuário não encontrado: " + username)));
    }
}




