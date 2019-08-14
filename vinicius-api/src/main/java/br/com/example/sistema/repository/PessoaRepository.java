package br.com.example.sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.example.sistema.model.Pessoa;

public interface PessoaRepository extends JpaRepository<Pessoa,Long>{

}
