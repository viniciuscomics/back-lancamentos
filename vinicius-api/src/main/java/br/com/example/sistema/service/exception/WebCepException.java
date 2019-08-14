package br.com.example.sistema.service.exception;

import lombok.Getter;

public class WebCepException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Getter
	private String msgErroAdicional;
	
	public WebCepException() {
		super("CEP inexistente. Verifique o CEP e tente novamente.");
	}
	
	public WebCepException(String msg, String msgErroAdicional){		
		super(msg);
		this.msgErroAdicional = msgErroAdicional;
	}
}
