package br.com.example.sistema.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class WebCepDto {
	private String logradouro;
	private String bairro;	
	private String localidade;
	private String cep;
	private String complemento;
	private String ibge;
	private String gia;
	private String uf;
	private boolean erro;
}
