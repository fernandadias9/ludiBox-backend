package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.enums.EnumDocumento;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.enums.EnumStatus;
import br.com.ludibox.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private ImagemService imagemService;

    public void salvarImagemPessoa(MultipartFile imagem, Integer idPessoa) throws LudiBoxException {

        Pessoa pessoaComImagem = pessoaRepository.
                findById(idPessoa)
                .orElseThrow(() -> new LudiBoxException("Erro: ", "Usuario não encontrado", HttpStatus.INTERNAL_SERVER_ERROR));
        String imagemBase64 = imagemService.processarImagem(imagem);
        pessoaComImagem.setImagemUsuarioEmBase64(imagemBase64);
        pessoaRepository.save(pessoaComImagem);
    }

    public Pessoa salvar(Pessoa pessoa) throws LudiBoxException {
        verificarPessoaExistente(pessoa);

        validarDocumento(pessoa);

        return pessoaRepository.save(pessoa);
    }

    private void validarDocumento(Pessoa pessoa) throws LudiBoxException {
        EnumDocumento tipo = pessoa.getTipoDocumento();
        String documento = pessoa.getValorDocumento();

        if(tipo.equals(EnumDocumento.CNPJ)){
            if (documento.length() != 14){
                throw new LudiBoxException("CNPJ: ", "O valor inserido é inválido! ", HttpStatus.BAD_REQUEST);
            }
        }else if(tipo.equals(EnumDocumento.CPF)){
            if (documento.length() != 11){
                throw new LudiBoxException("CPF: ", "O valor inserido é inválido! ", HttpStatus.BAD_REQUEST);
            }
        }
    }

    public void verificarPessoaExistente(Pessoa pessoa) throws LudiBoxException {
        List<Pessoa> pessoas = pessoaRepository.findAll();

        for (Pessoa pessoaValidada : pessoas) {
            if (pessoa.getValorDocumento().equals(pessoaValidada.getValorDocumento())) {
                throw new LudiBoxException("Documento: ", "Documento já cadastrado!", HttpStatus.BAD_REQUEST);
            }else if (pessoa.getEmail().equals(pessoaValidada.getEmail())) {
                throw new LudiBoxException("Email: ", "Email já cadastrado!", HttpStatus.BAD_REQUEST);
            } else if (pessoa.getTelefone().equals(pessoaValidada.getTelefone())) {
                throw new LudiBoxException("Telefone: ", "Telefone já cadastrado!", HttpStatus.BAD_REQUEST);
            }
        }
    }

    public Pessoa cadastrarAdm(Pessoa pessoa) throws LudiBoxException{
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        if (pessoaAutenticada.getPerfil() == EnumPerfil.USUARIO) {
            throw new LudiBoxException("Administração: ", "Ação exclusiva para administradores!", HttpStatus.UNAUTHORIZED);
        }
        verificarPessoaExistente(pessoa);
        pessoa.setPerfil(EnumPerfil.ADMINISTRADOR);
        return pessoaRepository.save(pessoa);
    }

    public List<Pessoa> buscarTodos() throws LudiBoxException{
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();

        if (pessoaAutenticada.getPerfil() == EnumPerfil.USUARIO) {
            throw new LudiBoxException("Administração: ", "Ação exclusiva para administradores!", HttpStatus.UNAUTHORIZED);
        }

        return pessoaRepository.findAll();
    }

    public Pessoa atualizarDados(Pessoa pessoa) throws LudiBoxException{
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        verificarDados(pessoa);
        if (pessoaAutenticada.getId() != pessoa.getId()) {
            throw new LudiBoxException("Erro: ", "Usuários só podem alterar seus próprios dados!", HttpStatus.UNAUTHORIZED);
        }
        return pessoaRepository.save(pessoa);
    }

    private void verificarDados(Pessoa pessoa) throws LudiBoxException {
        Pessoa pessoaVerificada = pessoaRepository.findById(pessoa.getId()).get();
        if(!pessoaVerificada.getValorDocumento().equals(pessoa.getValorDocumento())) {
            throw new LudiBoxException("Documento: ", "O documento não pode ser alterado!", HttpStatus.BAD_REQUEST);
        }
    }

    public void desativarPessoa(Pessoa pessoa) throws LudiBoxException{
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        if (pessoaAutenticada.getId() != pessoa.getId()) {
            throw new LudiBoxException("Erro: ", "Usuários só podem alterar seus próprios dados!", HttpStatus.UNAUTHORIZED);
        }

        Pessoa pessoaDesativada = pessoaRepository.findById(pessoa.getId()).get();
        pessoaDesativada.setSituacao(EnumStatus.INATIVO);
        pessoaRepository.save(pessoaDesativada);
    }

    public void reativarPessoa(Pessoa pessoa) throws LudiBoxException{
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();

        if (pessoaAutenticada.getPerfil() == EnumPerfil.USUARIO) {
            throw new LudiBoxException("Administração: ", "Ação exclusiva para administradores!", HttpStatus.UNAUTHORIZED);
        }


        Pessoa pessoaAtivada = pessoaRepository.findById(pessoa.getId()).get();
        pessoaAtivada.setSituacao(EnumStatus.ATIVO);
        pessoaRepository.save(pessoaAtivada);
    }

    public void bloquearPessoaFisica(Pessoa pessoa) throws LudiBoxException{
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();

        if (pessoaAutenticada.getPerfil() == EnumPerfil.USUARIO) {
            throw new LudiBoxException("Administração: ", "Ação exclusiva para administradores!", HttpStatus.UNAUTHORIZED);
        }

        Pessoa pessoaBloqueada = pessoaRepository.findById(pessoa.getId()).get();
        pessoaBloqueada.setSituacao(EnumStatus.BLOQUEADO);
        pessoaRepository.save(pessoaBloqueada);
    }

}
