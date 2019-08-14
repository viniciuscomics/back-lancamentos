package br.com.example.sistema.event.listener;

import java.net.URI;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.example.sistema.event.RecursoCriadoEvento;

@Component
public class RecursoCriadoListener implements ApplicationListener<RecursoCriadoEvento>{

	@Override
	public void onApplicationEvent(RecursoCriadoEvento event) {
		
		HttpServletResponse response = event.getResponse();
		Long id = event.getId();		
		adicionarHeader(response, id);
	}

	private void adicionarHeader(HttpServletResponse response, Long id) {
		URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}")
				.buildAndExpand(id).toUri();
		
		response.setHeader("Location", uri.toASCIIString());
	}

	
}
