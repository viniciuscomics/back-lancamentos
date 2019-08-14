package br.com.example.sistema.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.github.javafaker.Faker;

import br.com.example.sistema.dto.WebCepDto;
import br.com.example.sistema.service.exception.WebCepException;

@Service
public class WebCepService {

	public WebCepDto getCepDto(String cep) throws WebCepException {

		RestTemplate rest = new RestTemplate();
		ResponseEntity<WebCepDto> dto = new ResponseEntity<WebCepDto>(HttpStatus.OK);
		try {
			dto = rest.getForEntity("http://viacep.com.br/ws/" + cep + "/json", WebCepDto.class);

			if (dto.hasBody()) {

				if (dto.getBody().isErro()) {
					throw new WebCepException();
				} else {
					return dto.getBody();
				}
			} else {
				throw new WebCepException("Erro na requisição de CEP.", dto.getStatusCode().getReasonPhrase());
			}
		} catch (RestClientException e) {

			throw new WebCepException("Erro na requisição", e.getMessage());
		}
	}

	public void foo() {
		
		
	}

}

class teste {

	private static void foo1() {
		WebCepService service = new WebCepService();

		try {
			WebCepDto dto = service.getCepDto("04856650");

			System.out.println(dto.getLogradouro());
			System.out.println(dto.getBairro());
			System.out.println(dto.getCep());
			System.out.println(dto.getLocalidade());
			System.out.println(dto.getComplemento());
			System.out.println(dto.getUf());
			System.out.println(dto.getIbge());
		} catch (WebCepException e) {
			System.out.println(e.getMessage());
			if (e.getMsgErroAdicional() != null && !e.getMsgErroAdicional().isEmpty()) {
				System.out.println("Msg Erro Adicional = " + e.getMsgErroAdicional());
			}
		}
	}
	
	public static void main(String[] args) {
		
		WebCepService service = new WebCepService();
		service.foo();
		

	}
}
