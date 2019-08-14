package br.com.example.sistema.ambiente.dev;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import br.com.example.sistema.model.Endereco;
import br.com.example.sistema.model.Pessoa;
import br.com.example.sistema.service.PessoaService;
import br.com.example.sistema.utils.Utils;
import lombok.Getter;

@Component
public class PopularBasePessoas {

	Logger log = LoggerFactory.getLogger(PopularBasePessoas.class);

	@Autowired
	private PessoaService pessoaService;

	@PostConstruct
	private void init() {
		cadastrarPessoasFake();
	}

	public void cadastrarPessoasFake() {

//		Faker faker = null;
//		List<Pessoa> listPessoa = new ArrayList<>();
//
//		for (int i = 0; i < 3; i++) {
//
//			faker = new Faker();
//			Pessoa pessoa = new Pessoa();
//			Endereco endereco = new Endereco();
//
//			pessoa.setAtivo(true);
//
//			pessoa.setId(faker.number().randomNumber());
//			pessoa.setNome(faker.name().fullName());
//
//			endereco.setBairro(faker.address().firstName());
//			endereco.setCep(faker.address().zipCode());
//			endereco.setCidade(faker.address().city());
//			endereco.setLogradouro(faker.address().streetName());
//			endereco.setNumero(faker.address().streetAddressNumber());
//			pessoa.setEndereco(endereco);
//
//			listPessoa.add(pessoa);
//
//		}
//		int cont = 0;
//		for (Pessoa ps : listPessoa) {
//
//			System.out.println("Exibindo dados pessoa -> " + (++cont));
//			Utils.executeFunctionGet(Pessoa.class, ps);
//			System.out.println();
//			System.out.println();
//			// System.out.println(ToStringBuilder.reflectionToString(ps));
//		}
	}
}

@Getter
class A {
	private String a = "aaa";
	private B b = new B();
}

@Getter
class B {
	private String b = "bbb";
	private C c = new C();
}

@Getter
class C {
	private String c = "ccc";
	private D d = new D();
}

@Getter
class D {
	private String d = "ddd";
	private E e = new E();
}

@Getter
class E {
	private String e = "eee";
	private F f = new F();
}

@Getter
class F {

	private String f = "fff";
	private String g = "ggg";
	private String h = "hhh";
}

class testePessoa {
	static Logger log = LoggerFactory.getLogger(testePessoa.class);

	public static void main(String[] args) {
		PopularBasePessoas ps = new PopularBasePessoas();
		ps.cadastrarPessoasFake();
	}
}