package br.com.ludibox.controller;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.PerfilDTO;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.service.PessoaService;
import io.jsonwebtoken.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/pessoa")
public class PessoaController {

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private AuthenticationService authService;

    @PreAuthorize("hasAuthority('USUARIO')")
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
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    @PostMapping("/cadastrar_adm")
    public ResponseEntity<Pessoa> cadastrarAdm(@RequestBody Pessoa pessoa) throws LudiBoxException{
        return ResponseEntity.ok(pessoaService.cadastrarAdm(pessoa));
    }
    @PreAuthorize("hasAuthority('USUARIO') or hasAuthority('ADMINISTRADOR')")
    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<Pessoa> atualizarPessoa(
            @PathVariable int id,
            @RequestBody Map<String, Object> pessoaDetails,
            BindingResult bindingResult) throws LudiBoxException {

        Optional<Pessoa> pessoaOptional = Optional.ofNullable(pessoaService.buscarPorId(id));

        if (!pessoaOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Pessoa pessoa = pessoaOptional.get();
        pessoaService.atualizarDados(pessoa, pessoaDetails);
        if (bindingResult.hasErrors()) {
            throw new LudiBoxException("Erro: ", "Erro de validação nos dados enviados", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(pessoa);
    }

    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    @PutMapping("/desativar/{id}")
    public void desativarPessoa(@PathVariable int id) throws LudiBoxException {
        pessoaService.desativarPessoa(id);
    }
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    @PutMapping("/reativar/{id}")
    public void reativarPessoa(@PathVariable int id) throws LudiBoxException {
        pessoaService.reativarPessoa(id);
    }
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    @PutMapping("/bloquear/{id}")
    public void bloquearPessoa(@PathVariable int id) throws LudiBoxException {
        pessoaService.bloquearPessoaFisica(id);
    }
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    @GetMapping
    public List<Pessoa> buscarTodasPessoas() throws LudiBoxException{
        List<Pessoa> pessoas = pessoaService.buscarTodos();
        return pessoas;
    }
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    @GetMapping("/buscar_perfil/{id}")
    public PerfilDTO buscarPerfilPorId(@PathVariable int id) {
        PerfilDTO perfil = pessoaService.buscarPerfilPorId(id);
        return perfil;
    }
}
