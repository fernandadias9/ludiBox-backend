package br.com.ludibox.controller;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.CepDTO;
import br.com.ludibox.model.entity.Endereco;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.service.CepService;
import br.com.ludibox.service.EnderecoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/endereco")
public class EnderecoController {
	
	@Autowired
	private EnderecoService enderecoService;

	@Autowired
	private CepService cepService;
	
	@PostMapping("/novo-endereco")
	public ResponseEntity<Endereco> salvarEnderecoParaPessoa(@RequestBody Endereco novo) throws LudiBoxException{
		return ResponseEntity.ok(enderecoService.salvarEnderecoParaPessoa(novo));
	}


	@PatchMapping("/atualizar-endereco/{id}")
    public ResponseEntity<Endereco> atualizarEnderecoParaPessoa(
			@PathVariable int id,
			@RequestBody Map<String, Object> enderecoDetails,
			BindingResult bindingResult) throws LudiBoxException {

		Optional<Endereco> enderecoOptional = Optional.ofNullable(enderecoService.buscarPorId(id));

		if (!enderecoOptional.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		Endereco endereco = enderecoOptional.get();
		enderecoService.atualizarEnderecoPessoa(endereco, enderecoDetails);
		if (bindingResult.hasErrors()) {
			throw new LudiBoxException("Erro: ", "Erro de validação de dados enviados", HttpStatus.BAD_REQUEST);
		}

        return ResponseEntity.ok(endereco);
    }

	@GetMapping("/buscar_por_cep/{cep}")
	public CepDTO buscarEnderecoPorCep(@PathVariable Integer cep) throws LudiBoxException {
		CepDTO cepDTO = cepService.buscarEnderecoPorCep(cep);
		return cepDTO;
	}

	@GetMapping("/pessoa/{pessoaId}")
	public List<Endereco> buscarEnderecosPorPessoa(@PathVariable Integer pessoaId) {
		return enderecoService.listarEnderecosPorPessoa(pessoaId);
	}

	@DeleteMapping("/deletar-endereco/{id}")
	public ResponseEntity<Void> deletarEndereco(@PathVariable Integer id) throws LudiBoxException {
		enderecoService.deletar(id);
		return ResponseEntity.noContent().build();
	}

}
