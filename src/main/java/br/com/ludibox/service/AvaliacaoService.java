package br.com.ludibox.service;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Avaliacao;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.ProdutoLocacao;
import br.com.ludibox.model.enums.StatusLocacao;
import br.com.ludibox.model.repository.AvaliacaoRepository;
import br.com.ludibox.model.repository.PessoaRepository;
import br.com.ludibox.model.repository.ProdutoLocacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AvaliacaoService {
    @Autowired
    ProdutoLocacaoRepository produtoLocacaoRepository;

    @Autowired
    AvaliacaoRepository avaliacaoRepository;

    @Autowired
    PessoaRepository pessoaRepository;

    @Transactional
    public Avaliacao salvar(Integer produtoLocacaoId, Integer avaliadorId, int estrelas) {
        ProdutoLocacao pl = produtoLocacaoRepository.findById(produtoLocacaoId)
                .orElseThrow(() -> new LudiBoxException("produtoLocacaoId", "ProdutoLocacao não encontrado", HttpStatus.NOT_FOUND));

        if (pl.getAvaliado()) {
            throw new LudiBoxException("avaliado", "Este produto já foi avaliado", HttpStatus.BAD_REQUEST);
        }
        if (!pl.getLocacao().getStatus().equals(StatusLocacao.PAGO)) {
            throw new LudiBoxException("status", "Locação não está com status PAGO", HttpStatus.BAD_REQUEST);
        }
        if (pl.getDataFim().isAfter(LocalDate.now())) {
            throw new LudiBoxException("dataFim", "Só é possível avaliar após dataFim", HttpStatus.BAD_REQUEST);
        }
        if (!pl.getLocacao().getLocador().getId().equals(avaliadorId)) {
            throw new LudiBoxException("avaliadorId", "Somente o locador pode avaliar", HttpStatus.FORBIDDEN);
        }

        Pessoa avaliador = pessoaRepository.findById(avaliadorId)
                .orElseThrow(() -> new LudiBoxException("avaliadorId", "Pessoa não encontrada", HttpStatus.NOT_FOUND));

        Avaliacao aval = new Avaliacao();
        aval.setEstrelas(estrelas);
        aval.setProdutoLocacao(pl);
        aval.setAvaliador(avaliador);

        // marca como avaliado
        pl.setAvaliado(true);
        produtoLocacaoRepository.save(pl);

        return avaliacaoRepository.save(aval);
    }

    @Transactional
    public Avaliacao alterar(Long id, int novasEstrelas, Long avaliadorId) {
        Avaliacao aval = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new LudiBoxException("id", "Avaliação não encontrada", HttpStatus.NOT_FOUND));

        if (!aval.getAvaliador().getId().equals(avaliadorId) || !aval.getAtivo()) {
            throw new LudiBoxException("autorizacao", "Operação não permitida", HttpStatus.FORBIDDEN);
        }
        aval.setEstrelas(novasEstrelas);
        return avaliacaoRepository.save(aval);
    }

    @Transactional
    public void deletar(Long id, Long avaliadorId) {
        Avaliacao aval = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new LudiBoxException("id", "Avaliação não encontrada", HttpStatus.NOT_FOUND));

        if (!aval.getAvaliador().getId().equals(avaliadorId) || !aval.getAtivo()) {
            throw new LudiBoxException("autorizacao", "Operação não permitida", HttpStatus.FORBIDDEN);
        }
        // soft delete
        aval.setAtivo(false);
        avaliacaoRepository.save(aval);
    }
}
