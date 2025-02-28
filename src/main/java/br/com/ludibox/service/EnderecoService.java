package br.com.ludibox.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Endereco;
import br.com.ludibox.model.repository.EnderecoRepository;

@Service
public class EnderecoService {
	
	@Autowired
	private EnderecoRepository enderecoRepository;

	 @Autowired
	 private AuthenticationService authService;

	 @Autowired
	 private CepService cepService;


	
	 
/*	public Endereco salvarEnderecoParaPf(Endereco novo) throws LudiBoxException {
		PessoaFisica pfAutenticada = authService.getPessoaFisicaAutenticada();

		Endereco enderecoCepValidado = new Endereco();

		enderecoCepValidado = cepService.validarCep(novo);


		validarPessoaFisica(pfAutenticada);
			validarNumeroDePf(enderecoCepValidado);
			novo.setPessoaFisica(pfAutenticada);
			pfAutenticada.getEnderecos().add(enderecoCepValidado);
			
			return enderecoRepository.save(novo);
		}*/
	 
	/*public Endereco salvarEnderecoParaPj(Endereco novo) throws LudiBoxException {
		PessoaJuridica pjAutenticada = authService.getPessoaJuridicaAutenticada();
		Endereco enderecoValidado = new Endereco();
		Endereco enderecoCepValidado = new Endereco();

		validarPessoaJuridica(pjAutenticada);
		validarNumeroDePj(novo);

		enderecoCepValidado = cepService.validarCep(novo);
		
		enderecoValidado = pessoaJaPossuiEndereco(enderecoCepValidado);

		enderecoValidado.setPessoaJuridica(pjAutenticada);
		pjAutenticada.setEndereco(enderecoValidado);

	
		return enderecoRepository.save(enderecoValidado);
	}
	
	public Endereco atualizarEnderecoPf(Endereco endereco) throws LudiBoxException{
    	PessoaFisica pessoaAutenticada = authService.getPessoaFisicaAutenticada();   	
    	validarPessoaFisica(pessoaAutenticada);
		validarNumeroDePf(endereco);
    	if (pessoaAutenticada.getId() != endereco.getPessoaFisica().getId()) {
			throw new LudiBoxException("Usuários só podem alterar seus próprios dados!", HttpStatus.BAD_REQUEST);
		}
    	return enderecoRepository.save(endereco);
    }
	
	public Endereco atualizarEnderecoPj(Endereco endereco) throws LudiBoxException{
    	PessoaJuridica pessoaAutenticada = authService.getPessoaJuridicaAutenticada();   	
    	validarPessoaJuridica(pessoaAutenticada);
		validarNumeroDePj(endereco);
    	if (pessoaAutenticada.getId() != endereco.getPessoaJuridica().getId()) {
			throw new LudiBoxException("Usuários só podem alterar seus próprios dados!", HttpStatus.BAD_REQUEST);
		}
    	return enderecoRepository.save(endereco);
    }
	
	public Endereco pessoaJaPossuiEndereco(Endereco novo) throws LudiBoxException {
		PessoaJuridica pjAutenticada = authService.getPessoaJuridicaAutenticada();
		Endereco existente = pjAutenticada.getEndereco();

		if(existente != null) {
			novo.setId(existente.getId());
		}
		return novo;
	}
*/
	/*private void validarPessoaFisica(PessoaFisica pessoaValidada) throws LudiBoxException {
	    pessoaFisicaRepository.findById(pessoaValidada.getId())
	        .orElseThrow(() -> new LudiBoxException("Pessoa física com ID " + pessoaValidada.getId() + " não encontrada!", HttpStatus.INTERNAL_SERVER_ERROR));
	}

	private void validarPessoaJuridica(PessoaJuridica pessoaValidada) throws LudiBoxException {
	    pessoaJuridicaRepository.findById(pessoaValidada.getId())
	        .orElseThrow(() -> new LudiBoxException("Pessoa jurídica com ID " + pessoaValidada.getId() + " não encontrada!", HttpStatus.INTERNAL_SERVER_ERROR));
	}*/

	/*public void validarNumeroDePf(Endereco enderecoValidado) throws LudiBoxException {
	    Integer numero = enderecoValidado.getNumero();
	    if (enderecoValidado.isSemNumero() && numero != null && numero != 0) {
	        throw new LudiBoxException("Número não foi solicitado!", HttpStatus.BAD_REQUEST);
	    } else if (!enderecoValidado.isSemNumero() && (numero == null || numero == 0)) {
	        throw new LudiBoxException("Número não foi adicionado!", HttpStatus.BAD_REQUEST);
	    }
	}

	public void validarNumeroDePj(Endereco enderecoValidado) throws LudiBoxException {
	    Integer numero = enderecoValidado.getNumero();
	    if (numero == null || numero == 0) {
	        throw new LudiBoxException("Número não foi adicionado!", HttpStatus.BAD_REQUEST);
	    }
	}
*/


	

}
