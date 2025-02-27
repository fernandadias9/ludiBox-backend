package br.com.ludibox.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class LudiBoxException extends Exception {

	HttpStatus httpStatus;
	
	public LudiBoxException(String mensagem, HttpStatus httpStatus) {
		super(mensagem);
		this.httpStatus=httpStatus;
	}

}
