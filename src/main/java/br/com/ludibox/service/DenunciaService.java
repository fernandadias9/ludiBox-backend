package br.com.ludibox.service;

import br.com.ludibox.model.dto.DenunciaDTO;
import br.com.ludibox.model.entity.Denuncia;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.enums.EnumStatusDenuncia;
import br.com.ludibox.model.enums.EnumStatusProdutoDenunciado;
import br.com.ludibox.model.repository.DenunciaRepository;
import br.com.ludibox.model.repository.PessoaRepository;
import br.com.ludibox.model.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final ProdutoRepository produtoRepository;
    private final PessoaRepository pessoaRepository;

    public Denuncia criar(DenunciaDTO dto) {
        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com ID: " + dto.getProdutoId()));

        var pessoa = pessoaRepository.findById(dto.getDenuncianteId())
                .orElseThrow(() -> new IllegalArgumentException("Denunciante não encontrado com ID: " + dto.getDenuncianteId()));

        Denuncia denuncia = new Denuncia();
        denuncia.setProduto(produto);
        denuncia.setMotivo(dto.getMotivo());
        denuncia.setDescricao(dto.getDescricao());
        denuncia.setDenunciante(pessoa);
        denuncia.setStatus(EnumStatusDenuncia.NOVO);
        denuncia.setStatusProdutoDenunciado(EnumStatusProdutoDenunciado.PENDENTE);
        denuncia.setDataCriacao(LocalDateTime.now());

        return denunciaRepository.save(denuncia);
    }
}
