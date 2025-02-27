package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Endereco;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.enums.EnumStatus;
import br.com.ludibox.model.repository.PessoaFisicaRepository;
import br.com.ludibox.model.repository.PessoaJuridicaRepository;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.Data;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Data
@Service
public class PessoaJuridicaService {

    @Autowired
    private PessoaJuridicaRepository pessoaJuridicaRepository;
    
    @Autowired
    private PessoaFisicaRepository pessoaFisicaRepository;
    
    @Autowired
    private AuthenticationService authService;
    
    @Autowired
    private ImagemService imagemService;

	@Autowired
	private EnderecoService enderecoService;

	@Autowired
	private CepService cepService;

	@Autowired
	private CnpjService cnpjService;

	@Autowired
	private Validator validator;
    
    
    public void salvarImagemPessoa(MultipartFile imagem, Integer idPessoa) throws LudiBoxException {
		
		PessoaJuridica pessoaComImagem = pessoaJuridicaRepository.
				findById(idPessoa)
				.orElseThrow(() -> new LudiBoxException("Usuario não encontrado", HttpStatus.BAD_REQUEST));
		String imagemBase64 = imagemService.processarImagem(imagem);
		pessoaComImagem.setImagemUsuarioEmBase64(imagemBase64);
		pessoaJuridicaRepository.save(pessoaComImagem);
	}
    
    public PessoaJuridica salvar(PessoaJuridica pessoaJuridica) throws LudiBoxException {
		try {

			Endereco enderecoCepValidado = new Endereco();

			enderecoCepValidado = cepService.validarCep(pessoaJuridica.getEndereco());

			cnpjService.validarCepAndCnpj(pessoaJuridica);

			pessoaJuridica.setEndereco(enderecoCepValidado);
			enderecoCepValidado.setPessoaJuridica(pessoaJuridica);

			verificarPessoaExistente(pessoaJuridica);

			return pessoaJuridicaRepository.save(pessoaJuridica);

		} catch (ConstraintViolationException e){
			throw new LudiBoxException("Erro ao salvar usuário: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
    }
    
    public PessoaJuridica atualizarDados(PessoaJuridica pessoaJuridica) throws LudiBoxException{
    	PessoaJuridica pessoaAutenticada = authService.getPessoaJuridicaAutenticada();   	
    	
    	verificarDados(pessoaJuridica);
    	if (pessoaAutenticada.getId() != pessoaJuridica.getId()) {
			throw new LudiBoxException("Usuários só podem alterar seus próprios dados!", HttpStatus.BAD_REQUEST);
		}
    	
    	return pessoaJuridicaRepository.save(pessoaJuridica);
    }
    
    
    public void desativarPessoaJuridica(PessoaJuridica pessoaJuridica) throws LudiBoxException{
    	PessoaJuridica pessoaAutenticada = authService.getPessoaJuridicaAutenticada();
    	
    	if (pessoaAutenticada.getId() != pessoaJuridica.getId()) {
			throw new LudiBoxException("Usuários só podem alterar seus próprios dados!", HttpStatus.BAD_REQUEST);
		}
    	
    	PessoaJuridica pessoa = pessoaJuridicaRepository.findById(pessoaJuridica.getId()).get();
    	pessoa.setSituacao(EnumStatus.INATIVO);
    	pessoaJuridicaRepository.save(pessoa);
    }
    
    
    public void reativarPessoaJuridica(PessoaJuridica pessoaJuridica) throws LudiBoxException{
    	PessoaJuridica pjAutenticada = authService.getPessoaJuridicaAutenticada();
    	
    	if (pjAutenticada.getPerfil() == EnumPerfil.USUARIO) {
    		throw new LudiBoxException("Ação exclusiva para administradores!", HttpStatus.BAD_REQUEST);
		}
    	
    	PessoaJuridica pessoa = pessoaJuridicaRepository.findById(pessoaJuridica.getId()).get();
    	pessoa.setSituacao(EnumStatus.ATIVO);
    	pessoaJuridicaRepository.save(pessoa);
    }
    
    
    public void bloquearPessoaJuridica(PessoaJuridica pessoaJuridica) throws LudiBoxException{
    	PessoaJuridica pjAutenticada = authService.getPessoaJuridicaAutenticada();
    	
    	if (pjAutenticada.getPerfil() == EnumPerfil.USUARIO) {
    		throw new LudiBoxException("Ação exclusiva para administradores!", HttpStatus.UNAUTHORIZED);
		}
    	
    	PessoaJuridica pessoa = pessoaJuridicaRepository.findById(pessoaJuridica.getId()).get();
    	pessoa.setSituacao(EnumStatus.BLOQUEADO);
    	pessoaJuridicaRepository.save(pessoa);
    }
    
    public List<PessoaJuridica> buscarTodosPj() throws LudiBoxException{
    	PessoaJuridica pjAutenticada = authService.getPessoaJuridicaAutenticada();
    	
    	if (pjAutenticada.getPerfil() == EnumPerfil.USUARIO) {
    		throw new LudiBoxException("Ação exclusiva para administradores!", HttpStatus.UNAUTHORIZED);
		}
    	return pessoaJuridicaRepository.findAll();
    }
    
    private void verificarDados(PessoaJuridica pessoaJuridica) throws LudiBoxException {
    	PessoaJuridica pessoaVerificada = pessoaJuridicaRepository.findById(pessoaJuridica.getId()).get();
		if(!pessoaVerificada.getCnpj().equals(pessoaJuridica.getCnpj())) {
			throw new LudiBoxException("O CNPJ não pode ser alterado!", HttpStatus.BAD_REQUEST);
		}		
	}

    public void verificarPessoaExistente(PessoaJuridica pessoaJuridica) throws LudiBoxException{
    	List<PessoaJuridica> pessoasPj = pessoaJuridicaRepository.findAll(); 
    	List<PessoaFisica> pessoasPf = pessoaFisicaRepository.findAll();
    	
    	for (PessoaJuridica pessoaValidada : pessoasPj) {
    		if (pessoaJuridica.getCnpj().equals(pessoaValidada.getCnpj())) {
    			throw new LudiBoxException("CNPJ já cadastrado!", HttpStatus.BAD_REQUEST);
			}else if (pessoaJuridica.getEmail().equals(pessoaValidada.getEmail())) {
				throw new LudiBoxException("Email já cadastrado!", HttpStatus.BAD_REQUEST);
			} else if (pessoaJuridica.getTelefone().equals(pessoaValidada.getTelefone())) {
				throw new LudiBoxException("Telefone já cadastrado!", HttpStatus.BAD_REQUEST);
			}
			
		}    
    	
    	for (PessoaFisica pessoaPfValidada : pessoasPf) {
    		if (pessoaJuridica.getEmail().equals(pessoaPfValidada.getEmail())) {
				throw new LudiBoxException("Email já cadastrado!", HttpStatus.BAD_REQUEST);
			} else if (pessoaJuridica.getTelefone().equals(pessoaPfValidada.getTelefone())) {
				throw new LudiBoxException("Telefone já cadastrado!", HttpStatus.BAD_REQUEST);
			}
			
		}  
    }

	

}
