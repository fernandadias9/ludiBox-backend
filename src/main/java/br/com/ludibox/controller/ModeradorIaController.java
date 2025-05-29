package br.com.ludibox.controller;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.service.IA.ValidadorConteudoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/public/ia")
public class ModeradorIaController {

    private final ValidadorConteudoService validadorConteudoService;

    @Autowired
    public ModeradorIaController(ValidadorConteudoService validadorConteudoService) {
        this.validadorConteudoService = validadorConteudoService;
    }

    @PostMapping(path = "/testar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> testarIA(@RequestBody @Valid Produto produto) {
        try {
            Produto resultado = validadorConteudoService.validar(produto);
            return ResponseEntity.ok(resultado);

        } catch (LudiBoxException e) {
            return ResponseEntity
                    .status(e.getHttpStatus())
                    .body(Map.of(
                            "erro", e.getCampo(),
                            "detalhes", e.getMensagem()
                    ));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "erro", "Erro interno",
                            "detalhes", e.getMessage()
                    ));
        }
    }
}
