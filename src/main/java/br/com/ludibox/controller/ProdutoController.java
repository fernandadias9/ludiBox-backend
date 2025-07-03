package br.com.ludibox.controller;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.ProdutoDetalheDto;
import br.com.ludibox.model.dto.ProdutoListarDto;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.enums.StatusProduto;
import br.com.ludibox.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/produto")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> criarProduto(@RequestPart("produto") @Valid Produto produto,
                                               @RequestPart("imagens") List<MultipartFile> imagens) {
        try {
            produtoService.salvar(produto, imagens);
            return ResponseEntity.status(HttpStatus.CREATED).body("Anúncio criado com sucesso");
        } catch (LudiBoxException e) {
            return ResponseEntity.status(e.getHttpStatus()).body("Não foi possível criar anúncio: " + e.getMensagem());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar imagens");
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> atualizarProduto(@PathVariable Integer id,
                                                   @RequestPart("produto") @Valid Produto produto,
                                                   @RequestPart(value = "imagens", required = false) List<MultipartFile> imagens) {
        try {
            produtoService.atualizar(id, produto, imagens);
            return ResponseEntity.ok("Produto atualizado com sucesso");
        } catch (LudiBoxException e) {
            return ResponseEntity.status(e.getHttpStatus()).body("Não foi possível atualizar o produto: " + e.getMensagem());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar imagens");
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> atualizarStatus(@PathVariable Integer id, @RequestParam StatusProduto status) {
        String mensagemSucesso = "";
        String mensagemErro = "";

        if(status == StatusProduto.ATIVO) {
            mensagemSucesso = "Anúncio ativado com sucesso";
            mensagemErro = "Não foi possível ativar anúncio";
        }

        if(status == StatusProduto.INATIVO) {
            mensagemSucesso = "Anúncio inativado com sucesso";
            mensagemErro = "Não foi possível inativar anúncio";
        }
        try {
            produtoService.atualizarStatus(id, status);
            return ResponseEntity.ok(mensagemSucesso);
        } catch (LudiBoxException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(mensagemErro + ": " + e.getMensagem());
        }
    }

    @PutMapping("/bloqueio/{id}")
    public ResponseEntity<String> atualizarBloqueio(@PathVariable Integer id) {
        try {
            produtoService.atualizarBloqueio(id);
            return ResponseEntity.ok("Bloqueio atualizado com sucesso");
        } catch (LudiBoxException e) {
            return ResponseEntity.status(e.getHttpStatus()).body("Não foi possível atualizar bloqueio." + e.getMensagem());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarProduto(@PathVariable Integer id) {
        try {
            produtoService.deletarProduto(id);
            return ResponseEntity.ok("Anúncio excluído com sucesso");
        } catch (LudiBoxException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(e.getMensagem());
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<List<ProdutoListarDto>> listarTodos() {
        List<ProdutoListarDto> produtos = produtoService.buscarTodos();
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/listarComFiltro")
    public ResponseEntity<Page<ProdutoListarDto>> listarTodos(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cidade,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProdutoListarDto> produtos = produtoService.buscarComFiltro(nome, cidade, pageable);
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/buscar/{id}")
    public ProdutoDetalheDto buscarProduto(@PathVariable Integer id) {
        return produtoService.buscar(id);
    }

    @GetMapping("/pessoa/{pessoaId}")
    public ResponseEntity<List<Produto>> listarPorUsuario(@PathVariable Integer pessoaId) {
        List<Produto> produtos = produtoService.listarPorUsuario(pessoaId);
        return ResponseEntity.ok(produtos);
    }
}
