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

	public Endereco atualizarEnderecoPessoa(Endereco endereco, Map<String, Object> enderecoDetails) throws LudiBoxException {
		Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
		if (pessoaAutenticada.getId() != endereco.getPessoa().getId()) {
			throw new LudiBoxException("Endereço: ", "Usuários só podem alterar seus próprios dados!", HttpStatus.BAD_REQUEST);
		}

		if (enderecoDetails.containsKey("nome")) {
			endereco.setNome((String) enderecoDetails.get("nome"));
		}
		if (enderecoDetails.containsKey("cep")) {
			Object cepValue = enderecoDetails.get("cep");
			endereco.setCep(cepValue instanceof Integer ? (Integer) cepValue : Integer.parseInt(cepValue.toString()));
		}
		if (enderecoDetails.containsKey("rua")) {
			endereco.setRua((String) enderecoDetails.get("rua"));
		}
		if (enderecoDetails.containsKey("numero")) {
			Object numeroValue = enderecoDetails.get("numero");
			if (numeroValue == null || numeroValue.toString().isEmpty()) {
				endereco.setNumero(null);
			} else {
				endereco.setNumero(numeroValue instanceof Integer ? (Integer) numeroValue : Integer.parseInt(numeroValue.toString()));
			}
		}
		if (enderecoDetails.containsKey("complemento")) {
			endereco.setComplemento((String) enderecoDetails.get("complemento"));
		}
		if (enderecoDetails.containsKey("bairro")) {
			endereco.setBairro((String) enderecoDetails.get("bairro"));
		}
		if (enderecoDetails.containsKey("cidade")) {
			endereco.setCidade((String) enderecoDetails.get("cidade"));
		}
		if (enderecoDetails.containsKey("estado")) {
			endereco.setEstado((String) enderecoDetails.get("estado"));
		}
		if (enderecoDetails.containsKey("semNumero")) {
			endereco.setSemNumero((Boolean) enderecoDetails.get("semNumero"));
		}

		return enderecoRepository.save(endereco);
	}

	public Endereco buscarPorId(int id) {
		return enderecoRepository.findById(id).orElseThrow(() -> new LudiBoxException("Endereço com ID: " + id, " Não foi encontrado", HttpStatus.BAD_REQUEST));
	}

	public List<Endereco> listarEnderecosPorPessoa(Integer idPessoa) {
		return enderecoRepository.findByPessoaIdAndNotDeleted(idPessoa);
	}

	public void deletar(Integer idEndereco) throws LudiBoxException {
		Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
		Endereco endereco = enderecoRepository.findById(idEndereco)
				.orElseThrow(() -> new LudiBoxException("Endereço com ID: " + idEndereco, " não foi encontrado", HttpStatus.BAD_REQUEST));

		if (!endereco.getPessoa().getId().equals(pessoaAutenticada.getId())) {
			throw new LudiBoxException("Endereço: ", "Usuário não autorizado a deletar este endereço!", HttpStatus.FORBIDDEN);
		}

		if (endereco.isDeleted()) {
			throw new LudiBoxException("Endereço: ", "Este endereço já foi removido!", HttpStatus.BAD_REQUEST);
		}

		endereco.delete();
		enderecoRepository.save(endereco);
	}

	public Endereco buscarPorId(Integer id) {
		return enderecoRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Endereço não encontrado com id: " + id));
	}
}
