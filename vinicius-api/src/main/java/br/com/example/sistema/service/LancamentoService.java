package br.com.example.sistema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.example.sistema.model.Lancamento;
import br.com.example.sistema.model.Pessoa;
import br.com.example.sistema.repository.LancamentoRepository;
import br.com.example.sistema.service.exception.PessoaInexistenteOuInativaException;

@Service
public class LancamentoService {

	@Autowired
	private PessoaService pessoaService;
	
	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	public Lancamento salvar(Lancamento lancamento) {
		
		Pessoa pessoa = pessoaService.buscarPessoa(lancamento.getPessoa().getId());
		
		if(pessoa == null || !pessoa.getAtivo()) {
			throw new PessoaInexistenteOuInativaException();
		}
		return lancamentoRepository.save(lancamento);
	}
	
}
