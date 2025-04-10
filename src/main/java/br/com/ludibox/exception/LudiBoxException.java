package br.com.ludibox.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Data
public class LudiBoxException extends RuntimeException {

	HttpStatus httpStatus;
	String campo;
	String mensagem;

	public LudiBoxException(String campo, String mensagem, HttpStatus httpStatus) {
		super(mensagem);
		this.campo = campo;
		this.mensagem = mensagem;
		this.httpStatus = httpStatus;
	}


}
