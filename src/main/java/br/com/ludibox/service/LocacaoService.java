package br.com.ludibox.service;

import br.com.ludibox.model.dto.ValorBrutoMesDTO;
import br.com.ludibox.model.entity.*;
import br.com.ludibox.model.enums.StatusLocacao;
import br.com.ludibox.model.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
        Optional<Locacao> locacaoExistente = this.buscarLocacaoPendentePorUsuarioId(locacao.getLocador().getId());
        if (locacaoExistente.isPresent()) {
            throw new IllegalStateException("Erro ao iniciar locação.");
        }

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

    public Locacao buscarPorId(Integer id) {
        return locacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Locação não encontrada"));
    }

    @Transactional
    public void finalizarLocacao(Integer locacaoId, Integer enderecoId, Integer locadorId) {
        Locacao locacao = locacaoRepository.findById(locacaoId)
                .orElseThrow(() -> new RuntimeException("Locação não encontrada"));

        if (!locacao.getLocador().getId().equals(locadorId)) {
            throw new RuntimeException("Locação não pertence ao usuário");
        }

        escolherEnderecoEntrega(locacaoId, enderecoId, locadorId);

        locacao.setStatus(StatusLocacao.PAGO);
        locacao.setDataHoraPagamento(LocalDateTime.now());

        locacaoRepository.save(locacao);
    }

    public List<ProdutoLocacao> obterLocacoesRecebidas(Integer usuarioId) {
        List<ProdutoLocacao> locacoes = produtoLocacaoRepository.findLocacoesRecebidas(usuarioId);
        return locacoes;
    }

    public List<Locacao> obterLocacoesEfetuadas(Integer usuarioId) {
        return locacaoRepository.findLocacoesEfetuadas(usuarioId);
    }

    public List<Locacao> obterTodasAsLocacoes() {
        return locacaoRepository.findAll();
    }

    public List<Locacao> filtrarLocacoes(LocalDate dataInicio, LocalDate dataFim, Double valorMin, Double valorMax) {
        List<Locacao> todas = locacaoRepository.findAll();

        return todas.stream()
                .filter(loc -> {
                    boolean dentroPeriodo = true;
                    boolean dentroValor = true;

                    if (dataInicio != null) {
                        LocalDateTime inicio = dataInicio.atStartOfDay();
                        dentroPeriodo &= loc.getDataHoraEfetuada() != null && !loc.getDataHoraEfetuada().isBefore(inicio);
                    }
                    if (dataFim != null) {
                        LocalDateTime fim = dataFim.atTime(23, 59, 59);
                        dentroPeriodo &= loc.getDataHoraEfetuada() != null && !loc.getDataHoraEfetuada().isAfter(fim);
                    }
                    if (valorMin != null) {
                        dentroValor &= loc.getValorTotal() != null && loc.getValorTotal() >= valorMin;
                    }
                    if (valorMax != null) {
                        dentroValor &= loc.getValorTotal() != null && loc.getValorTotal() <= valorMax;
                    }

                    return dentroPeriodo && dentroValor;
                })
                .toList();
    }

    public List<ValorBrutoMesDTO> listarValorBrutoMensal(String dataInicioStr, String dataFimStr) {
        LocalDate dataInicio = (dataInicioStr != null && !dataInicioStr.isEmpty()) ? LocalDate.parse(dataInicioStr) : null;
        LocalDate dataFim = (dataFimStr != null && !dataFimStr.isEmpty()) ? LocalDate.parse(dataFimStr) : null;

        List<Locacao> locacoes = this.filtrarLocacoes(dataInicio, dataFim, null, null);

        Map<String, Double> mapaMesValor = new HashMap<>();

        for (Locacao loc : locacoes) {
            if (loc.getDataHoraEfetuada() == null || loc.getValorTotal() == null) continue;

            String mesAno = String.format("%02d/%d", loc.getDataHoraEfetuada().getMonthValue(), loc.getDataHoraEfetuada().getYear());

            double valorAtual = mapaMesValor.getOrDefault(mesAno, 0.0);
            mapaMesValor.put(mesAno, valorAtual + loc.getValorTotal());
        }

        List<ValorBrutoMesDTO> listaDTO = new ArrayList<>();

        for (Map.Entry<String, Double> entry : mapaMesValor.entrySet()) {
            ValorBrutoMesDTO dto = new ValorBrutoMesDTO();
            dto.setMesAno(entry.getKey());
            dto.setValor(entry.getValue());
            listaDTO.add(dto);
        }

        listaDTO.sort((a, b) -> {
            try {
                String[] partsA = a.getMesAno().split("/");
                String[] partsB = b.getMesAno().split("/");

                int yearA = Integer.parseInt(partsA[1]);
                int monthA = Integer.parseInt(partsA[0]);

                int yearB = Integer.parseInt(partsB[1]);
                int monthB = Integer.parseInt(partsB[0]);

                if (yearA != yearB) {
                    return Integer.compare(yearB, yearA);
                } else {
                    return Integer.compare(monthB, monthA);
                }
            } catch (Exception e) {
                return b.getMesAno().compareTo(a.getMesAno());
            }
        });

        return listaDTO;
    }
}