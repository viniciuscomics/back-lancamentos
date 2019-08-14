package br.com.example.sistema.Resource;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.example.sistema.event.RecursoCriadoEvento;
import br.com.example.sistema.model.Categoria;
import br.com.example.sistema.repository.CategoriaRepository;

@RestController
@RequestMapping("/categorias")
public class CategoriaResource {

	@Autowired
	private CategoriaRepository categoriaRepository;
	
	@Autowired
	ApplicationEventPublisher publisher;
	
	private final String ROLE_CADASTRAR_CATEGORIA = "ROLE_CADASTRAR_CATEGORIA";
	private final String ROLE_PESQUISAR_CATEGORIA = "ROLE_PESQUISAR_CATEGORIA";
	
	
	@GetMapping
	@PreAuthorize("hasAuthority('"+ROLE_PESQUISAR_CATEGORIA+"')")
	public List<Categoria> listar(){
		return categoriaRepository.findAll();
	}
	
	@PostMapping
	@PreAuthorize("hasAuthority('"+ROLE_CADASTRAR_CATEGORIA+"')")
	public ResponseEntity<Categoria> criar(@Valid @RequestBody Categoria categoria, HttpServletResponse response) {
		Categoria cat = categoriaRepository.save(categoria);
		
		publisher.publishEvent(new RecursoCriadoEvento(this, response, cat.getId()));
		
		return ResponseEntity.status(HttpStatus.CREATED).body(cat);
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('"+ROLE_PESQUISAR_CATEGORIA+"')")
	public ResponseEntity<?> buscarPeloCodigo(@PathVariable Long id) {	
		Optional<Categoria> cat = categoriaRepository.findById(id);
		return cat.orElse(null) != null ? ResponseEntity.ok(cat) : ResponseEntity.noContent().build();
	}
	
}
