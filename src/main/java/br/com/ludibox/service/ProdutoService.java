package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Endereco;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.repository.ProdutoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private AuthenticationService authService;

    private static final int MAX_IMAGENS = 5;
    private static final long MAX_TAMANHO_IMAGEM = 2 * 1024 * 1024;

    public void salvar(@Valid Produto produto, List<MultipartFile> imagens) throws LudiBoxException, IOException {
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        produto.setAnunciante(pessoaAutenticada);
        if (produto.getNome() == null || produto.getDescricao() == null || produto.getEstoque() == null || produto.getPreco() <= 0.0 || produto.getPreco() == null) {
            throw new LudiBoxException("Campos obrigatórios", "Campos obrigatórios não foram completamente preenchidos", HttpStatus.BAD_REQUEST);
        }

        if (imagens != null && imagens.size() > MAX_IMAGENS) {
            throw new LudiBoxException("Imagens", "Número máximo de imagens excedido. Máximo permitido: " + MAX_IMAGENS, HttpStatus.BAD_REQUEST);
        }

        List<String> imagensBase64 = new ArrayList<>();
        for (MultipartFile imagem : imagens) {
            if (imagem.getSize() > MAX_TAMANHO_IMAGEM) {
                throw new LudiBoxException("Imagens", "Tamanho máximo da imagem excedido. Máximo permitido: " + MAX_TAMANHO_IMAGEM + " bytes", HttpStatus.BAD_REQUEST);
            }
            imagensBase64.add(Base64.getEncoder().encodeToString(imagem.getBytes()));
        }
        produto.setImagens(imagensBase64);

        produtoRepository.save(produto);
    }
}
