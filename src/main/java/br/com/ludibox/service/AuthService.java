package br.com.ludibox.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.ludibox.model.repository.PessoaFisicaRepository;
import br.com.ludibox.model.repository.PessoaJuridicaRepository;

@Service
public class AuthService implements UserDetailsService {
    
    @Autowired
    private PessoaFisicaRepository pessoaFisicaRepository;
    
    @Autowired
    private PessoaJuridicaRepository pessoaJuridicaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return pessoaFisicaRepository.findByEmail(username)
                .map(user -> (UserDetails) user)
                .orElseGet(() -> pessoaJuridicaRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username)));
    }
}



