package br.com.example.sistema.repository;

import  org.springframework.data.jpa.repository.JpaRepository;
import br.com.example.sistema.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria,Long>{

}
