package br.com.example.sistema.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import br.com.example.sistema.model.Usuario;

public class UsuarioSistema extends User{	
	
	private static final long serialVersionUID = 1L;
	
	private Usuario user;
	
	public UsuarioSistema(Usuario user, Collection<? extends GrantedAuthority> authorities) {
		super(user.getEmail(), user.getSenha(), authorities);
		this.user = user;
	}
	
	public Usuario getUser() {
		return user;
	}

}
