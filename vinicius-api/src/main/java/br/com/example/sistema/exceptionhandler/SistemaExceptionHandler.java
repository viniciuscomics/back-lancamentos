package br.com.example.sistema.exceptionhandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class SistemaExceptionHandler extends ResponseEntityExceptionHandler{
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		String msgUser = messageSource.getMessage("mensagem.invalida",null,LocaleContextHolder.getLocale());
		String msgDev = ex.getCause().toString();
		
		return handleExceptionInternal(ex, Arrays.asList(new Erro(msgUser,msgDev)), headers, HttpStatus.BAD_REQUEST, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		// TODO Auto-generated method stub
		return handleExceptionInternal(ex, criarListaErros(ex.getBindingResult()), headers, HttpStatus.BAD_REQUEST, request);
	 
	}
	
	@ExceptionHandler({DataIntegrityViolationException.class})
	public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request){
	
		String msgUser = messageSource.getMessage("recurso.operacao-nao-permitida",null,LocaleContextHolder.getLocale());
		String msgDev = ExceptionUtils.getRootCauseMessage(ex);
		
		return handleExceptionInternal(ex, Arrays.asList(new Erro(msgUser,msgDev)),  new HttpHeaders() , HttpStatus.BAD_REQUEST, request);
	}
	
	@ExceptionHandler({EmptyResultDataAccessException.class})	
	public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex, WebRequest request) {
		String msgUser = messageSource.getMessage("recurso.nao-encontrado",null,LocaleContextHolder.getLocale());
		String msgDev = getStringCause(ex);
		return handleExceptionInternal(ex, Arrays.asList(new Erro(msgUser,msgDev)), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}
	
	@ExceptionHandler({NoSuchElementException.class})	
	public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException ex, WebRequest request) {
		String msgUser = messageSource.getMessage("recurso.nao-encontrado",null,LocaleContextHolder.getLocale());
		String msgDev = getStringCause(ex);
		return handleExceptionInternal(ex, Arrays.asList(new Erro(msgUser,msgDev)), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}
	
	private String getStringCause(RuntimeException ex) {		
		return ex.getCause() == null?ex.toString():ex.getCause().toString();
	}
	
	private List<Erro> criarListaErros(BindingResult binding){
		List<Erro> erros = new ArrayList<>();
		
		for(FieldError field : binding.getFieldErrors()) {
			String msgUser  = messageSource.getMessage(field, LocaleContextHolder.getLocale());			
			erros.add(new Erro(msgUser,field.toString()));	
		}		
		
		return erros;
	}
	
	public static class Erro{
		String msgUser;
		String msgDev;
		public Erro(String msgUser, String msgDev) {
			super();
			this.msgUser = msgUser;
			this.msgDev = msgDev;
		}
		public String getMsgUser() {
			return msgUser;
		}
		public void setMsgUser(String msgUser) {
			this.msgUser = msgUser;
		}
		public String getMsgDev() {
			return msgDev;
		}
		public void setMsgDev(String msgDev) {
			this.msgDev = msgDev;
		}
		
		
	}

}
