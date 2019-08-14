package br.com.example.sistema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.example.sistema.model.Pessoa;
import br.com.example.sistema.repository.PessoaRepository;

@Service
public class PessoaService {

	@Autowired
	PessoaRepository pessoaRepository;
	
	public Pessoa buscarPessoa(Long id) {				
		return pessoaRepository.findById(id).orElse(null);
	}	
}
