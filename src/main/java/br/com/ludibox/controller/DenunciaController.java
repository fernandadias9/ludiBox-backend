package br.com.ludibox.controller;

import br.com.ludibox.model.dto.DenunciaDTO;
import br.com.ludibox.model.dto.DenunciaListarDTO;
import br.com.ludibox.model.dto.DenuncianteDTO;
import br.com.ludibox.model.entity.Denuncia;
import br.com.ludibox.model.enums.EnumMotivoDenuncia;
import br.com.ludibox.model.enums.EnumStatusDenuncia;
import br.com.ludibox.model.enums.EnumStatusProdutoDenunciado;
import br.com.ludibox.model.enums.StatusProduto;
import br.com.ludibox.model.repository.DenunciaRepository;
import br.com.ludibox.model.repository.ProdutoRepository;
import br.com.ludibox.service.DenunciaService;
import br.com.ludibox.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/denuncias")
@RequiredArgsConstructor
public class DenunciaController {

    private final DenunciaRepository denunciaRepository;
    private final ProdutoRepository produtoRepository;
    private final DenunciaService denunciaService;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<Denuncia> criar(@Valid @RequestBody DenunciaDTO dto) {
        Denuncia denunciaSalva = denunciaService.criar(dto);
        return ResponseEntity.ok(denunciaSalva);
    }

    @GetMapping
    public ResponseEntity<List<Denuncia>> listarTodas() {
        List<Denuncia> denuncias = denunciaRepository.findAll();
        return ResponseEntity.ok(denuncias);
    }

    @GetMapping("/filtro")
    public ResponseEntity<List<DenunciaListarDTO>> filtrar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) EnumMotivoDenuncia motivo,
            @RequestParam(required = false) EnumStatusDenuncia status
    ) {
        LocalDateTime inicio = (dataInicio != null) ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = (dataFim != null) ? dataFim.atTime(23, 59, 59, 999_999_999) : null;

        List<Denuncia> denuncias = denunciaRepository.buscarComFiltros(inicio, fim, motivo, status);

        List<DenunciaListarDTO> resultado = denuncias.stream().map(denuncia -> {
            DenuncianteDTO denuncianteDTO = null;
            if (denuncia.getDenunciante() != null) {
                denuncianteDTO = new DenuncianteDTO();
                denuncianteDTO.setId(denuncia.getDenunciante().getId());
                denuncianteDTO.setNome(denuncia.getDenunciante().getNome());
            }

            String nomeDenunciado = null;
            Integer produtoId = null;
            EnumStatusProdutoDenunciado produtoStatus = denuncia.getStatusProdutoDenunciado();

            if (denuncia.getProduto() != null) {
                produtoId = denuncia.getProduto().getId();

                if (denuncia.getProduto().getAnunciante() != null) {
                    nomeDenunciado = denuncia.getProduto().getAnunciante().getNome();
                }
            }

            return new DenunciaListarDTO(
                    denuncia.getId(),
                    denuncia.getMotivo(),
                    denuncia.getDescricao(),
                    denuncia.getStatus(),
                    denuncia.getDataCriacao(),
                    denuncianteDTO,
                    nomeDenunciado,
                    produtoId,
                    produtoStatus
            );
        }).toList();

        return ResponseEntity.ok(resultado);
    }

    @PutMapping("/{id}/permitir")
    public ResponseEntity<?> permitir(@PathVariable Integer id) {
        return denunciaRepository.findById(id).map(denuncia -> {
            denuncia.getProduto().setStatus(StatusProduto.ATIVO);
            produtoRepository.save(denuncia.getProduto());

            denuncia.setStatusProdutoDenunciado(EnumStatusProdutoDenunciado.PERMITIDO);
            denuncia.setStatus(EnumStatusDenuncia.ANALISADO);

            denunciaRepository.save(denuncia);

            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/bloquear")
    public ResponseEntity<?> bloquear(@PathVariable Integer id) {
        return denunciaRepository.findById(id).map(denuncia -> {
            denuncia.getProduto().setStatus(StatusProduto.BLOQUEADO);
            produtoRepository.save(denuncia.getProduto());

            denuncia.setStatusProdutoDenunciado(EnumStatusProdutoDenunciado.BLOQUEADO);
            denuncia.setStatus(EnumStatusDenuncia.ANALISADO);
            denunciaRepository.save(denuncia);

            String destino = null;
            String nomeProduto = null;

            if (denuncia.getProduto() != null) {
                nomeProduto = denuncia.getProduto().getNome();

                if (denuncia.getProduto().getAnunciante() != null) {
                    destino = denuncia.getProduto().getAnunciante().getEmail();
                }
            }
            //Desativar para funcionar na faculdade.
            if (destino != null && nomeProduto != null) {
                emailService.enviarAnuncioBloqueado(destino, nomeProduto);
            }

            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
