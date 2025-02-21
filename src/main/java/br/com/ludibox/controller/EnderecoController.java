package br.com.ludibox.controller;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.CepDTO;
import br.com.ludibox.model.entity.Endereco;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.service.CepService;
import br.com.ludibox.service.EnderecoService;
import br.com.ludibox.service.PessoaFisicaService;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/endereco")
public class EnderecoController {
	
	@Autowired
	private EnderecoService enderecoService;

	@Autowired
	private CepService cepService;
	
	@PostMapping("/add_for_Pf")
	public ResponseEntity<Endereco> salvarEnderecoParaPf(@RequestBody Endereco novo) throws LudiBoxException{
		return ResponseEntity.ok(enderecoService.salvarEnderecoParaPf(novo));
	}
	
	@PostMapping("/add_for_Pj")
	public ResponseEntity<Endereco> salvarEnderecoParaPj(@RequestBody Endereco novo) throws LudiBoxException{
		return ResponseEntity.ok(enderecoService.salvarEnderecoParaPj(novo));
	}
	
	@PutMapping("/update_for_Pf")
    public ResponseEntity<Endereco> atualizarEnderecoParaPf(@RequestBody Endereco endereco) throws LudiBoxException {
        return ResponseEntity.ok(enderecoService.atualizarEnderecoPf(endereco));
    }
	
	@PutMapping("/update_for_Pj")
    public ResponseEntity<Endereco> atualizarEnderecoParaPj(@RequestBody Endereco endereco) throws LudiBoxException {
        return ResponseEntity.ok(enderecoService.atualizarEnderecoPj(endereco));
    }

	@GetMapping("/buscar_por_cep/{cep}")
	public CepDTO buscarEnderecoPorCep(@PathVariable Integer cep) throws LudiBoxException {
		CepDTO cepDTO = cepService.buscarEnderecoPorCep(cep);
		return cepDTO;
	}
    
	
	

}
