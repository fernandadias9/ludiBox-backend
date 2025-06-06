package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.ProdutoDetalheDto;
import br.com.ludibox.model.dto.ProdutoListarDto;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.enums.StatusProduto;
import br.com.ludibox.model.repository.ProdutoRepository;
import br.com.ludibox.service.IA.ValidadorConteudoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private ImagemService imagemService;

    @Autowired
    private ValidadorConteudoService validadorConteudoService;

    private static final int MAX_IMAGENS = 4;
    private static final long MAX_TAMANHO_IMAGEM = 2 * 1024 * 1024;

    public void salvar(@Valid Produto produto, List<MultipartFile> imagens) throws LudiBoxException, IOException {
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        produto.setAnunciante(pessoaAutenticada);

        if (imagens != null && imagens.size() > MAX_IMAGENS) {
            throw new LudiBoxException("Imagens", "Número máximo de imagens excedido. Máximo permitido: " + MAX_IMAGENS, HttpStatus.BAD_REQUEST);
        }

        List<String> imagensBase64 = new ArrayList<>();
        if (imagens != null) {
            for (MultipartFile imagem : imagens) {
                if (imagem.getSize() > MAX_TAMANHO_IMAGEM) {
                    throw new LudiBoxException("Imagens", "Tamanho máximo da imagem excedido. Máximo permitido: " + MAX_TAMANHO_IMAGEM + " bytes", HttpStatus.BAD_REQUEST);
                }
                String base64Imagem = imagemService.processarImagem(imagem);
                imagensBase64.add(base64Imagem);
            }
        }
        produto.setImagens(imagensBase64);

        validadorConteudoService.validar(produto);

        produtoRepository.save(produto);
    }

    public void atualizar(Integer id, @Valid Produto produtoAtualizado, List<MultipartFile> imagens) throws LudiBoxException, IOException {
        Pessoa pessoaAutenticada = this.authService.getPessoaAutenticada();

        Produto produtoExistente = validarProduto(id);

        if (pessoaAutenticada.getPerfil() == EnumPerfil.USUARIO && pessoaAutenticada != produtoExistente.getAnunciante()) {
            throw new LudiBoxException("Atualização não permitida", "Apenas o anunciante pode atualizar o anúncio.", HttpStatus.UNAUTHORIZED);
        }

        if (imagens != null && imagens.size() > MAX_IMAGENS) {
            throw new LudiBoxException("Imagens", "Número máximo de imagens excedido. Máximo permitido: " + MAX_IMAGENS, HttpStatus.BAD_REQUEST);
        }

        List<String> imagensBase64 = new ArrayList<>();
        if (imagens != null) {
            for (MultipartFile imagem : imagens) {
                if (imagem.getSize() > MAX_TAMANHO_IMAGEM) {
                    throw new LudiBoxException("Imagens", "Tamanho máximo da imagem excedido. Máximo permitido: " + MAX_TAMANHO_IMAGEM + " bytes", HttpStatus.BAD_REQUEST);
                }
                String base64Imagem = imagemService.processarImagem(imagem);
                imagensBase64.add(base64Imagem);
            }
        }

        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setDescricao(produtoAtualizado.getDescricao());
        produtoExistente.setPreco(produtoAtualizado.getPreco());
        produtoExistente.setAltura(produtoAtualizado.getAltura());
        produtoExistente.setLargura(produtoAtualizado.getLargura());
        produtoExistente.setComprimento(produtoAtualizado.getComprimento());
        produtoExistente.setDatasIndisponiveis(produtoAtualizado.getDatasIndisponiveis());
        produtoExistente.setStatus(produtoAtualizado.getStatus());

        if (!imagensBase64.isEmpty()) {
            produtoExistente.setImagens(imagensBase64);
        }

        if ((produtoAtualizado.getImagens() != null && !produtoAtualizado.getImagens().isEmpty()) || (imagensBase64 != null && !imagensBase64.isEmpty())) {
            List<String> imagensAtualizadas = new ArrayList<>();

            if (produtoAtualizado.getImagens() != null) {
                imagensAtualizadas.addAll(produtoAtualizado.getImagens());
            }

            if (imagensBase64 != null) {
                imagensAtualizadas.addAll(imagensBase64);
            }

            produtoExistente.setImagens(imagensAtualizadas);
        }

        produtoRepository.save(produtoExistente);
    }

    public void atualizarStatus(Integer id, StatusProduto novoStatus) throws LudiBoxException {
        Pessoa pessoaAutenticada = this.authService.getPessoaAutenticada();

        Produto produto = validarProduto(id);

        if (pessoaAutenticada != produto.getAnunciante()) {
            throw new LudiBoxException("Ação não autorizada.", "Apenas o anunciante pode ativar/desativar o anúncio.", HttpStatus.UNAUTHORIZED);
        }

        if (produto.getStatus() == StatusProduto.BLOQUEADO) {
            throw new LudiBoxException("Ação inválida", "Somente administradores podem modificar o status de anúncios bloqueados", HttpStatus.UNAUTHORIZED);
        }

        produto.setStatus(novoStatus);

        produtoRepository.save(produto);
    }

    public void atualizarBloqueio(Integer id) throws LudiBoxException {
        this.authService.verificarPermissaoAdmin();

        Produto produto = validarProduto(id);

        StatusProduto statusProduto = produto.getStatus();

        if (statusProduto == StatusProduto.BLOQUEADO) {
            produto.setStatus(StatusProduto.ATIVO);
        } else {
            produto.setStatus(StatusProduto.BLOQUEADO);
        }

        produtoRepository.save(produto);
    }

    public void deletarProduto(Integer id) throws LudiBoxException {
        Pessoa pessoaAutenticada = this.authService.getPessoaAutenticada();

        Produto produto = validarProduto(id);

        if (pessoaAutenticada.getPerfil() == EnumPerfil.USUARIO && pessoaAutenticada != produto.getAnunciante()) {
            throw new LudiBoxException("Exclusão não permitida", "Apenas o anunciante pode excluir o anúncio.", HttpStatus.UNAUTHORIZED);
        }

        this.produtoRepository.delete(produto);
    }

    public List<ProdutoListarDto> buscarTodos() {
        List<Produto> produtos = produtoRepository.buscarProdutosComAnuncianteAtivo();

        return produtos.stream()
                .filter(produto -> produto.getStatus() != StatusProduto.ATIVO)
                .map(produto -> {
            ProdutoListarDto dto = new ProdutoListarDto();

            dto.setId(produto.getId());
            dto.setNome(produto.getNome());
            dto.setPreco(produto.getPreco());
            dto.setIdAnunciante(produto.getAnunciante().getId());
            dto.setNomeAnunciante(produto.getAnunciante().getNome());
            dto.setImagemAnunciante(produto.getAnunciante().getImagemUsuarioEmBase64());
            dto.setImagem(produto.getImagens().isEmpty() ? null : produto.getImagens().get(0));

            return dto;
        }).collect(Collectors.toList());
    }

    public ProdutoDetalheDto buscar(Integer id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        ProdutoDetalheDto dto = new ProdutoDetalheDto();

        dto.setId(produto.getId());
        dto.setNome(produto.getNome());
        dto.setDataCadastro(produto.getDataCadastro());
        dto.setAltura(produto.getAltura());
        dto.setLargura(produto.getLargura());
        dto.setComprimento(produto.getComprimento());
        dto.setPesoSuportado(produto.getPesoSuportado());
        dto.setDescricao(produto.getDescricao());
        dto.setPreco(produto.getPreco());
        dto.setDatasIndisponiveis(produto.getDatasIndisponiveis());
        dto.setIdAnunciante(produto.getAnunciante().getId());
        dto.setNomeAnunciante(produto.getAnunciante().getNome());
        dto.setImagemAnunciante(produto.getAnunciante().getImagemUsuarioEmBase64());
        dto.setImagens(produto.getImagens());

        return dto;
    }

    protected Produto validarProduto(Integer id) throws LudiBoxException {
        Optional<Produto> produtoOpt = produtoRepository.findById(id);
        if (produtoOpt.isEmpty()) {
            throw new LudiBoxException("Anúncio não encontrado", "Anúncio com ID " + id + " não encontrado", HttpStatus.NOT_FOUND);
        }
        return produtoOpt.get();
    }

    public List<Produto> listarPorUsuario(Integer pessoaId) {
        return produtoRepository.findByAnuncianteId(pessoaId);
    }

    public Page<ProdutoListarDto> buscarComFiltro(String nome, Pageable pageable) {
        Page<Produto> produtos;

        if (nome != null && !nome.isBlank()) {
            produtos = produtoRepository.findByNomeContainingIgnoreCaseAndStatus(nome, StatusProduto.ATIVO, pageable);
        } else {
            produtos = produtoRepository.findByStatus(StatusProduto.ATIVO, pageable);
        }

        return produtos.map(produto -> {
            ProdutoListarDto dto = new ProdutoListarDto();
            dto.setId(produto.getId());
            dto.setNome(produto.getNome());
            dto.setPreco(produto.getPreco());
            dto.setIdAnunciante(produto.getAnunciante().getId());
            dto.setNomeAnunciante(produto.getAnunciante().getNome());
            dto.setImagemAnunciante(produto.getAnunciante().getImagemUsuarioEmBase64());
            dto.setImagem(produto.getImagens().isEmpty() ? null : produto.getImagens().get(0));
            return dto;
        });
    }
}
