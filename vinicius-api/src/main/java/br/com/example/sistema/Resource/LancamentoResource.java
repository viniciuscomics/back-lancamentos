package br.com.example.sistema.Resource;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.example.sistema.event.RecursoCriadoEvento;
import br.com.example.sistema.exceptionhandler.SistemaExceptionHandler.Erro;
import br.com.example.sistema.model.Lancamento;
import br.com.example.sistema.repository.LancamentoRepository;
import br.com.example.sistema.repository.filter.LancamentoFilter;
import br.com.example.sistema.repository.projection.ResumoLancamento;
import br.com.example.sistema.service.LancamentoService;
import br.com.example.sistema.service.exception.PessoaInexistenteOuInativaException;
import br.com.example.sistema.utils.Utils;

@RestController
@RequestMapping("/lancamentos")
public class LancamentoResource {

	@Autowired 
	LancamentoRepository lancamentoRepository;
	
	@Autowired
	private LancamentoService lancamentoService;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@Autowired
	MessageSource messageSource;
	
	private final String ROLE_CADASTRAR_LANCAMENTO = "ROLE_CADASTRAR_LANCAMENTO";
	private final String ROLE_REMOVER_LANCAMENTO = "ROLE_REMOVER_LANCAMENTO";
	private final String ROLE_PESQUISAR_LANCAMENTO = "ROLE_PESQUISAR_LANCAMENTO";
	
	@GetMapping
	@PreAuthorize("hasAuthority('"+ROLE_PESQUISAR_LANCAMENTO+"')")
	public Page<Lancamento> pesquisar(LancamentoFilter filter, Pageable page){
		return lancamentoRepository.filtrar(filter,page);
	}
	
	@GetMapping(params = "resumo")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and #oauth2.hasScope('read')")
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		return lancamentoRepository.resumir(lancamentoFilter, pageable);
	}
		
	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('"+ROLE_PESQUISAR_LANCAMENTO+"')")
	public ResponseEntity<?> getLancamento(@PathVariable Long id) {		
		Optional<Lancamento> lm = lancamentoRepository.findById(id);		
			
		return lm.orElse(null) !=null ? ResponseEntity.ok(lm) : ResponseEntity.notFound().build();
	}
	
	@PostMapping
	@PreAuthorize("hasAuthority('"+ROLE_CADASTRAR_LANCAMENTO+"')")
	public ResponseEntity<Lancamento> create(@Valid @RequestBody Lancamento lancamento, HttpServletResponse response) {
		
		Lancamento lc = lancamentoService.salvar(lancamento);
		publisher.publishEvent(new RecursoCriadoEvento(this, response, lc.getId()));
		return ResponseEntity.status(HttpStatus.CREATED).body(lc);		
	}
	
	@ExceptionHandler({PessoaInexistenteOuInativaException.class})
	public ResponseEntity<Object> handlePessoaInexistenteOuInativaException(PessoaInexistenteOuInativaException ex){
		String msgUser = messageSource.getMessage("pessoa.inexistente-ou-inativo",null,LocaleContextHolder.getLocale());
		String msgDev = ExceptionUtils.getRootCauseMessage(ex);
		
		return ResponseEntity.badRequest().body(Arrays.asList(new Erro(msgUser,msgDev))); 
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('"+ROLE_REMOVER_LANCAMENTO+"')")
	public void remover(@PathVariable Long id) {
		lancamentoRepository.deleteById(id);
	}
}
