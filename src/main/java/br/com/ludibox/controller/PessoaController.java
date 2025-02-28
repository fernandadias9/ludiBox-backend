package br.com.ludibox.controller;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.service.PessoaService;
import io.jsonwebtoken.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/pessoa")
public class PessoaController {

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private AuthenticationService authService;


    @PostMapping("/{idPessoa}/upload")
    public void UploadPessoa(@RequestParam("imagem") MultipartFile imagem, @PathVariable Integer idPessoa)
            throws LudiBoxException, IOException {
        if(imagem == null) {
            throw new LudiBoxException("Erro: ", "Arquivo inválido", HttpStatus.BAD_REQUEST);
        }

        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        if (pessoaAutenticada == null) {
            throw new LudiBoxException("Não autorizado: ", "Usuário sem permissão de acesso", HttpStatus.UNAUTHORIZED);
        }

        pessoaService.salvarImagemPessoa(imagem, idPessoa);

    }

    @PostMapping("/cadastrar_adm")
    public ResponseEntity<Pessoa> cadastrarAdm(@RequestBody Pessoa pessoa) throws LudiBoxException{
        return ResponseEntity.ok(pessoaService.cadastrarAdm(pessoa));
    }

    @PutMapping("/atualizar")
    public ResponseEntity<Pessoa> atualizarPessoa(@RequestBody Pessoa pessoa) throws LudiBoxException {
        return ResponseEntity.ok(pessoaService.atualizarDados(pessoa));
    }

    @PutMapping("/desativar")
    public void desativarPessoa(@RequestBody Pessoa pessoa) throws LudiBoxException {
        pessoaService.desativarPessoa(pessoa);
    }

    @PutMapping("/reativar")
    public void reativarPessoa(@RequestBody Pessoa pessoa) throws LudiBoxException {
        pessoaService.reativarPessoa(pessoa);
    }

    @PutMapping("/bloquear")
    public void bloquearPessoa(@RequestBody Pessoa pessoa) throws LudiBoxException {
        pessoaService.bloquearPessoaFisica(pessoa);
    }

    @GetMapping
    public List<Pessoa> buscarTodasPessoas() throws LudiBoxException{
        List<Pessoa> pessoas = pessoaService.buscarTodos();
        return pessoas;
    }
}
