package br.com.ludibox.service;

import br.com.ludibox.model.entity.*;
import br.com.ludibox.model.enums.StatusLocacao;
import br.com.ludibox.model.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LocacaoService {
    @Autowired
    private LocacaoRepository locacaoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoLocacaoRepository produtoLocacaoRepository;

    @Autowired
    private CancelamentoRepository cancelamentoRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    public Locacao abrirNovaLocacao(Locacao locacao) {
        if (locacao.getProdutos() == null || locacao.getProdutos().size() != 1) {
            throw new IllegalArgumentException("A locação inicial deve conter exatamente um produto.");
        }

        ProdutoLocacao produtoLocacao = locacao.getProdutos().get(0);
        produtoLocacao.setLocacao(locacao);

        long diasLocados = produtoLocacao.getDiasLocados();
        double valorTotal = diasLocados * produtoLocacao.getValorDiario();

        locacao.setValorTotal(valorTotal);
        locacao.setDataHoraEfetuada(LocalDateTime.now());
        locacao.setStatus(StatusLocacao.PENDENTE);
        locacao.setCancelado(false);

        Produto produto = produtoRepository.findById(produtoLocacao.getProduto().getId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        produtoLocacao.setProduto(produto);

        List<LocalDate> novasDatasIndisponiveis = produto.getDatasIndisponiveis() != null
                ? new ArrayList<>(produto.getDatasIndisponiveis())
                : new ArrayList<>();

        LocalDate atual = produtoLocacao.getDataInicio();
        while (!atual.isAfter(produtoLocacao.getDataFim())) {
            if (!novasDatasIndisponiveis.contains(atual)) {
                novasDatasIndisponiveis.add(atual);
            }
            atual = atual.plusDays(1);
        }

        produto.setDatasIndisponiveis(novasDatasIndisponiveis);
        produtoRepository.save(produto);

        return locacaoRepository.save(locacao);
    }

    @Transactional
    public Locacao incluirProdutoNaLocacao(Integer locacaoId, ProdutoLocacao novoProdutoLocacao) {
        Locacao locacao = locacaoRepository.findById(locacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada"));

        Produto produtoDoBanco = produtoRepository.findById(
                novoProdutoLocacao.getProduto().getId()
        ).orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        novoProdutoLocacao.setProduto(produtoDoBanco);

        novoProdutoLocacao.setLocacao(locacao);

        ProdutoLocacao produtoLocacaoSalvo = produtoLocacaoRepository.save(novoProdutoLocacao);

        List<LocalDate> datasIndisponiveis = produtoDoBanco.getDatasIndisponiveis() != null
                ? new ArrayList<>(produtoDoBanco.getDatasIndisponiveis())
                : new ArrayList<>();

        LocalDate atual = produtoLocacaoSalvo.getDataInicio();
        while (!atual.isAfter(produtoLocacaoSalvo.getDataFim())) {
            if (!datasIndisponiveis.contains(atual)) {
                datasIndisponiveis.add(atual);
            }
            atual = atual.plusDays(1);
        }
        produtoDoBanco.setDatasIndisponiveis(datasIndisponiveis);
        produtoRepository.save(produtoDoBanco);

        locacao.getProdutos().add(produtoLocacaoSalvo);
        locacao.setValorTotal(calcularValorTotal(locacao));

        return locacaoRepository.save(locacao);
    }


    @Transactional
    public Locacao retirarProdutoDaLocacao(Integer locacaoId, Integer produtoLocacaoId) {
        Locacao locacao = locacaoRepository.findById(locacaoId)
                .orElseThrow(() -> new RuntimeException("Locação não encontrada"));

        ProdutoLocacao produtoLocacao = locacao.getProdutos().stream()
                .filter(p -> p.getId().equals(produtoLocacaoId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Produto na locação não encontrado"));

        Produto produto = produtoLocacao.getProduto();

        List<LocalDate> datasIndisponiveis = produto.getDatasIndisponiveis();
        LocalDate atual = produtoLocacao.getDataInicio();

        while (!atual.isAfter(produtoLocacao.getDataFim())) {
            datasIndisponiveis.remove(atual);
            atual = atual.plusDays(1);
        }

        produto.setDatasIndisponiveis(datasIndisponiveis);
        produtoRepository.save(produto);

        locacao.getProdutos().remove(produtoLocacao);
        produtoLocacaoRepository.delete(produtoLocacao);
        locacao.setValorTotal(calcularValorTotal(locacao));

        return locacaoRepository.save(locacao);
    }

    public double calcularValorTotal(Locacao locacao) {
        return locacao.getProdutos().stream()
                .mapToDouble(prod -> prod.getValorDiario() * prod.getDiasLocados())
                .sum();
    }

    @Transactional
    public void deletarLocacao(Integer locacaoId) {
        Locacao locacao = locacaoRepository.findById(locacaoId)
                .orElseThrow(() -> new RuntimeException("Locação não encontrada"));

        for (ProdutoLocacao produtoLocacao : locacao.getProdutos()) {
            Produto produto = produtoLocacao.getProduto();

            List<LocalDate> datasIndisponiveis = produto.getDatasIndisponiveis();
            if (datasIndisponiveis == null) {
                datasIndisponiveis = new ArrayList<>();
            }

            LocalDate atual = produtoLocacao.getDataInicio();
            while (!atual.isAfter(produtoLocacao.getDataFim())) {
                datasIndisponiveis.remove(atual);
                atual = atual.plusDays(1);
            }

            produto.setDatasIndisponiveis(datasIndisponiveis);
            produtoRepository.save(produto);

            produtoLocacaoRepository.delete(produtoLocacao);
        }
        locacaoRepository.delete(locacao);
    }

    @Transactional
    public void cancelarLocacao(Integer locacaoId, String motivoCancelamento) {
        Locacao locacao = locacaoRepository.findById(locacaoId)
                .orElseThrow(() -> new RuntimeException("Locação não encontrada"));

        double multaValor = 0.0;

        LocalDate dataMaisProxima = locacao.getProdutos().stream()
                .map(ProdutoLocacao::getDataInicio)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now().plusDays(5));

        long diasDeAntecedencia = ChronoUnit.DAYS.between(LocalDate.now(), dataMaisProxima);

        if (diasDeAntecedencia >= 3) {
            locacao.setStatus(StatusLocacao.CANCELADO);
            locacao.setCancelado(true);
        } else {
            multaValor = locacao.getValorTotal() * 0.10;
            locacao.setStatus(StatusLocacao.CANCELADO);
            locacao.setCancelado(true);
        }

        locacao.getProdutos().forEach(this::liberarDatasDoProduto);

        Cancelamento cancelamento = new Cancelamento();
        cancelamento.setDataHoraCancelamento(LocalDateTime.now());
        cancelamento.setMotivo(motivoCancelamento);
        cancelamento.setMulta(multaValor);
        cancelamento.setLocacao(locacao);

        locacaoRepository.save(locacao);
    }

    private void liberarDatasDoProduto(ProdutoLocacao produtoLocacao) {
        Produto produto = produtoLocacao.getProduto();
        List<LocalDate> datasIndisponiveis = new ArrayList<>(produto.getDatasIndisponiveis());

        LocalDate atual = produtoLocacao.getDataInicio();
        while (!atual.isAfter(produtoLocacao.getDataFim())) {
            datasIndisponiveis.remove(atual); // remove se existir
            atual = atual.plusDays(1);
        }

        produto.setDatasIndisponiveis(datasIndisponiveis);
        produtoRepository.save(produto);
    }

    @Transactional
    public void escolherEnderecoEntrega(Integer locacaoId, Integer enderecoId, Integer locadorId) {
        Locacao locacao = locacaoRepository.findById(locacaoId)
                .orElseThrow(() -> new RuntimeException("Locação não encontrada"));

        Endereco endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));

        if (!endereco.getPessoa().getId().equals(locadorId)) {
            throw new RuntimeException("Endereço não pertence ao locador");
        }

        locacao.setEnderecoEntrega(endereco);
        locacaoRepository.save(locacao);
    }

    public Optional<Locacao> buscarLocacaoPendentePorUsuarioId(Integer usuarioId) {
        return locacaoRepository.findByUsuarioIdAndStatusPendente(usuarioId);
    }
}
