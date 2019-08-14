package br.com.example.sistema.Resource;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.example.sistema.event.RecursoCriadoEvento;
import br.com.example.sistema.model.Pessoa;
import br.com.example.sistema.repository.PessoaRepository;
import br.com.example.sistema.service.PessoaService;

@RestController
@RequestMapping("/pessoas")
public class PessoaResource {

	@Autowired
	PessoaRepository pessoaRepository;
	
	@Autowired
	PessoaService pessoaService;

	@Autowired
	private ApplicationEventPublisher publisher;
	
	private final String ROLE_CADASTRAR_PESSOA = "ROLE_CADASTRAR_PESSOA";
	private final String ROLE_REMOVER_PESSOA = "ROLE_REMOVER_PESSOA";
	private final String ROLE_PESQUISAR_PESSOA = "ROLE_PESQUISAR_PESSOA";


	@GetMapping
	@PreAuthorize("hasAuthority('"+ROLE_PESQUISAR_PESSOA+"')")
	public List<Pessoa> listar() {
		return pessoaRepository.findAll();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('"+ROLE_PESQUISAR_PESSOA+"')")
	public ResponseEntity<?> getPessoa(@PathVariable Long id) {
		Pessoa ps = pessoaService.buscarPessoa(id);
		return ps != null ? ResponseEntity.ok(ps) : ResponseEntity.notFound().build();
	}

	@PostMapping
	@PreAuthorize("hasAuthority('"+ROLE_CADASTRAR_PESSOA+"')")
	public ResponseEntity<Pessoa> cadastrar(@Valid @RequestBody Pessoa pessoa, HttpServletResponse response) {

		Pessoa prs = pessoaRepository.save(pessoa);

		publisher.publishEvent(new RecursoCriadoEvento(this, response, prs.getId()));

		return ResponseEntity.status(HttpStatus.CREATED).body(prs);

	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('"+ROLE_REMOVER_PESSOA+"')")
	public void remover(@PathVariable Long id) {
		pessoaRepository.deleteById(id);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('"+ROLE_CADASTRAR_PESSOA+"')")
	public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody Pessoa pessoa) {
		Pessoa pessoaSalva = pessoaService.buscarPessoa(id);
		BeanUtils.copyProperties(pessoa, pessoaSalva, "id");
		pessoaRepository.save(pessoaSalva);
		return pessoaSalva != null ? ResponseEntity.ok(pessoaSalva) : ResponseEntity.notFound().build();
	}
	
	@PutMapping("/{id}/ativo")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('"+ROLE_CADASTRAR_PESSOA+"')")
	public void atualizarPropriedadeAtivo(@PathVariable Long id,@RequestBody Boolean ativo) {
		Pessoa pessoaSalva = pessoaService.buscarPessoa(id);
		pessoaSalva.setAtivo(ativo);
		pessoaRepository.save(pessoaSalva);
	}

}
