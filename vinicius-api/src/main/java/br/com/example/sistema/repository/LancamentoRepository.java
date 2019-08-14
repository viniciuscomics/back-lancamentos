package br.com.example.sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.example.sistema.model.Lancamento;
import br.com.example.sistema.repository.lancamento.LancamentoRepositoryQuery;

public interface LancamentoRepository extends JpaRepository<Lancamento	,Long>, LancamentoRepositoryQuery{

}