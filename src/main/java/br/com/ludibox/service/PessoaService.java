package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.auth.RSAPasswordEncoder;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.PerfilDTO;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.enums.EnumStatus;
import br.com.ludibox.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private ImagemService imagemService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RSAPasswordEncoder passwordRsa;

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
        pessoa.setTelefone(validarTelefone(pessoa.getTelefone()));
        pessoa.setEmail(validarEmail(pessoa.getEmail()));
        String senhaCifrada = passwordEncoder.encode(pessoa.getSenha());
        pessoa.setSenha(senhaCifrada);

        return pessoaRepository.save(pessoa);
    }

    private String validarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            throw new LudiBoxException("Telefone: ", "Número não pode estar vazio!", HttpStatus.BAD_REQUEST);
        }
        if (!telefone.matches("^[0-9()\\s-]+$")) {
            throw new LudiBoxException("Telefone: ", "Número contém caracteres inválidos!", HttpStatus.BAD_REQUEST);
        }
        telefone = telefone.replaceAll("[^0-9]", "");
        if (telefone.length() != 10 && telefone.length() != 11) {
            throw new LudiBoxException("Telefone: ", "Número inserido é inválido!", HttpStatus.BAD_REQUEST);
        }

        return telefone;
    }

    private String validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new LudiBoxException("Email: ", "Email não pode estar vazio!", HttpStatus.BAD_REQUEST);
        }
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

        if (!email.matches(regex)) {
            throw new LudiBoxException("Email: ", "Email inserido é inválido!", HttpStatus.BAD_REQUEST);
        }

        return email;
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
        String senhaCifrada = passwordEncoder.encode(pessoa.getSenha());
        pessoa.setSenha(senhaCifrada);
        return pessoaRepository.save(pessoa);
    }

    public List<Pessoa> buscarTodos() throws LudiBoxException{
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();

        if (pessoaAutenticada.getPerfil() == EnumPerfil.USUARIO) {
            throw new LudiBoxException("Administração: ", "Ação exclusiva para administradores!", HttpStatus.UNAUTHORIZED);
        }

        return pessoaRepository.findAll();
    }

    public Pessoa atualizarDados(Pessoa pessoa, Map<String, Object> pessoaDetails) throws LudiBoxException{
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        verificarDados(pessoa);
        if (pessoaAutenticada.getId() != pessoa.getId()) {
            throw new LudiBoxException("Erro: ", "Usuários só podem alterar seus próprios dados!", HttpStatus.UNAUTHORIZED);
        }

        for (Map.Entry<String, Object> entry : pessoaDetails.entrySet()) {
            try {
                Field field = Pessoa.class.getDeclaredField(entry.getKey());
                field.setAccessible(true);

                field.set(pessoa, entry.getValue());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new LudiBoxException("Erro", "Campo inválido ou não acessível: " + entry.getKey(), HttpStatus.BAD_REQUEST);
            }
        }

        pessoa.setTelefone(validarTelefone(pessoa.getTelefone()));
        pessoa.setEmail(validarEmail(pessoa.getEmail()));

        return pessoaRepository.save(pessoa);
    }

    private void verificarDados(Pessoa pessoa) throws LudiBoxException {
        Pessoa pessoaVerificada = pessoaRepository.findById(pessoa.getId()).get();
        if(!pessoaVerificada.getValorDocumento().equals(pessoa.getValorDocumento())) {
            throw new LudiBoxException("Documento: ", "O documento não pode ser alterado!", HttpStatus.BAD_REQUEST);
        }
        if(!pessoa.getSituacao().equals(EnumStatus.ATIVO)){
            throw new LudiBoxException("Situação: ", "A situação não pode ser alterada!", HttpStatus.BAD_REQUEST);
        }
        if (!pessoa.getPerfil().equals(EnumPerfil.USUARIO)){
            throw new LudiBoxException("Perfil: ", "O tipo perfil não pode ser alterado!", HttpStatus.BAD_REQUEST);
        }
    }

    public void desativarPessoa(int id) throws LudiBoxException{
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        Pessoa pessoa = pessoaRepository.findById(id).orElseThrow(() -> new LudiBoxException("ID: ", "Pessoa não encontrada!", HttpStatus.BAD_REQUEST));

        if (pessoaAutenticada.getId() != pessoa.getId()) {
            throw new LudiBoxException("Erro: ", "Usuários só podem alterar seus próprios dados!", HttpStatus.UNAUTHORIZED);
        }

        Pessoa pessoaDesativada = pessoaRepository.findById(pessoa.getId()).get();
        pessoaDesativada.setSituacao(EnumStatus.INATIVO);
        pessoaRepository.save(pessoaDesativada);
    }

    public void reativarPessoa(int id) throws LudiBoxException{
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        Pessoa pessoa = pessoaRepository.findById(id).orElseThrow(() -> new LudiBoxException("ID: ", "Pessoa não encontrada!", HttpStatus.BAD_REQUEST));

        Pessoa pessoaAtivada = pessoaRepository.findById(pessoa.getId()).get();
        pessoaAtivada.setSituacao(EnumStatus.ATIVO);
        pessoaRepository.save(pessoaAtivada);
    }

    public void bloquearPessoaFisica(int id) throws LudiBoxException{
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        Pessoa pessoa = pessoaRepository.findById(id).orElseThrow(() -> new LudiBoxException("ID: ", "Pessoa não encontrada!", HttpStatus.BAD_REQUEST));


        if (pessoaAutenticada.getPerfil() == EnumPerfil.USUARIO) {
            throw new LudiBoxException("Administração: ", "Ação exclusiva para administradores!", HttpStatus.UNAUTHORIZED);
        }

        Pessoa pessoaBloqueada = pessoaRepository.findById(pessoa.getId()).get();
        pessoaBloqueada.setSituacao(EnumStatus.BLOQUEADO);
        pessoaRepository.save(pessoaBloqueada);
    }

    public Pessoa buscarPorId(int id){
        return pessoaRepository.findById(id).orElseThrow(() -> new LudiBoxException("ID: ", "Usuário não encontrado!", HttpStatus.BAD_REQUEST));
    }

    public PerfilDTO buscarPerfilPorId(int id){
        Pessoa pessoa = pessoaRepository.findById(id).orElseThrow(() -> new LudiBoxException("ID: ", "Usuário não encontrado!", HttpStatus.BAD_REQUEST));



        PerfilDTO perfil = new PerfilDTO();
        perfil.setNome(pessoa.getNome());
        perfil.setId(pessoa.getId());
        perfil.setImagemUsuarioEmBase64(pessoa.getImagemUsuarioEmBase64());
        perfil.setEmail(pessoa.getEmail());
        perfil.setSenha(passwordRsa.decode(pessoa.getPassword()));
        perfil.setTelefone(pessoa.getTelefone());
        perfil.setValorDocumento(pessoa.getValorDocumento());

        return perfil;
    }

}
