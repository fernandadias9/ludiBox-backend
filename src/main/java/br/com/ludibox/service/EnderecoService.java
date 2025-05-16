package br.com.ludibox.service;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Endereco;
import br.com.ludibox.model.repository.EnderecoRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Service
public class EnderecoService {
	
	 @Autowired
	 private EnderecoRepository enderecoRepository;

	 @Autowired
	 private AuthenticationService authService;

	 @Autowired
	 private CepService cepService;

	 @Autowired
	 private PessoaRepository pessoaRepository;

	public Endereco salvarEnderecoParaPessoa(Endereco novo) throws LudiBoxException {
		Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
		Endereco enderecoCepValidado = cepService.validarCep(novo);
		novo.setPessoa(pessoaAutenticada);
		pessoaAutenticada.getEnderecos().add(enderecoCepValidado);

		return enderecoRepository.save(novo);
	}

	public Endereco atualizarEnderecoPessoa(Endereco endereco, Map<String, Object> enderecoDetails) throws LudiBoxException{
    	Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
		if (pessoaAutenticada.getId() != endereco.getPessoa().getId()) {
			throw new LudiBoxException("Endereço: ", "Usuários só podem alterar seus próprios dados!", HttpStatus.BAD_REQUEST);
		}

		for (Map.Entry<String, Object> entry : enderecoDetails.entrySet()) {
			try {
				Field field = Endereco.class.getDeclaredField(entry.getKey());
				field.setAccessible(true);

				field.set(endereco, entry.getValue());
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new LudiBoxException("Erro", "Campo inválido ou não acessível: " + entry.getKey(), HttpStatus.BAD_REQUEST);
			}
		}

    	return enderecoRepository.save(endereco);
    }

	public Endereco buscarPorId(int id) {
		return enderecoRepository.findById(id).orElseThrow(() -> new LudiBoxException("Endereço com ID: " + id, " Não foi encontrado", HttpStatus.BAD_REQUEST));
	}

	public List<Endereco> listarEnderecosPorPessoa(Integer idPessoa) {
		return enderecoRepository.findByPessoaId(idPessoa);
	}

	public void deletar(Integer idEndereco) throws LudiBoxException {
		Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
		Endereco endereco = enderecoRepository.findById(idEndereco)
				.orElseThrow(() -> new LudiBoxException("Endereço com ID: " + idEndereco, " não foi encontrado", HttpStatus.BAD_REQUEST));

		if (!endereco.getPessoa().getId().equals(pessoaAutenticada.getId())) {
			throw new LudiBoxException("Endereço: ", "Usuário não autorizado a deletar este endereço!", HttpStatus.FORBIDDEN);
		}

		enderecoRepository.delete(endereco);
	}

}
