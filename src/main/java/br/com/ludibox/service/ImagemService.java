package br.com.ludibox.service;

import java.io.IOException;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.ludibox.exception.LudiBoxException;

@Service
public class ImagemService {

	
	public String processarImagem(MultipartFile file) throws LudiBoxException {
        // Converte MultipartFile em byte[]
        byte[] imagemBytes;
		try {
			imagemBytes = file.getBytes();
		} catch (IOException e) {
			throw new LudiBoxException("Erro ao processar arquivo", HttpStatus.INTERNAL_SERVER_ERROR);
		}
        
        // Converte byte[] para Base64
        String base64Imagem = Base64.getEncoder().encodeToString(imagemBytes);
        
        return base64Imagem;
    }
	
}
