package br.com.ludibox.controller;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
}
