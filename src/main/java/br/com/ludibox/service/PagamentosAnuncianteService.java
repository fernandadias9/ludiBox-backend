package br.com.ludibox.service;

import br.com.ludibox.model.dto.FiltroPagamentoDTO;
import br.com.ludibox.model.entity.PagamentosAnunciante;
import br.com.ludibox.model.repository.PagamentosAnuncianteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PagamentosAnuncianteService {

    @Autowired
    private PagamentosAnuncianteRepository repository;

    public PagamentosAnunciante salvar(PagamentosAnunciante novoPagamento) {
        PagamentosAnunciante pagamento = new PagamentosAnunciante();
        pagamento.setDataLimiteLiberacao(novoPagamento.getDataLimiteLiberacao());
        pagamento.setNomeAnunciante(novoPagamento.getNomeAnunciante());
        pagamento.setTipoChavePix(novoPagamento.getTipoChavePix());
        pagamento.setValorChavePix(novoPagamento.getValorChavePix());
        pagamento.setValor(novoPagamento.getValor());
        return repository.save(pagamento);
    }

    public List<PagamentosAnunciante> listarComFiltro(FiltroPagamentoDTO filtro) {
        if (filtro.getPago() != null) {
            return repository.findByPago(filtro.getPago());
        } else {
            return repository.findAll();
        }
    }

    public void marcarComoPago(Integer id) {
        PagamentosAnunciante pagamento = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
        pagamento.setPago(true);
        repository.save(pagamento);
    }
}
