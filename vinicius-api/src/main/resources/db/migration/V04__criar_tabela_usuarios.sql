CREATE TABLE usuario(
codigo BIGINT(20) PRIMARY KEY,
nome VARCHAR(50) NOT NULL,
email VARCHAR(50) NOT NULL,
senha VARCHAR(150) NOT NULL
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE permissao(
codigo BIGINT(20) PRIMARY KEY,
descricao VARCHAR(50) NOT NULL
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE usuario_permissao(
codigo_usuario BIGINT(20),
codigo_permissao BIGINT(20),
PRIMARY KEY(codigo_usuario,codigo_permissao),
FOREIGN KEY(codigo_usuario) REFERENCES usuario(codigo),
FOREIGN KEY(codigo_permissao) REFERENCES permissao(codigo) 
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO usuario(codigo, nome, email, senha) values (1, 'admin','admin@adm.com','$2a$10$Jcn2mpK0yBNhZlDwEbm.yu3d3kI1nq4SXm5vrxAHvJo44C6F20gsW');
INSERT INTO usuario(codigo, nome, email, senha) values (2, 'teste1','teste1@adm.com','$2a$10$eUWJsCUWCiG72PJOInrNq.jhkjebf1nnkE.GZN8KNYIS5aaE26RFC');

INSERT INTO permissao(codigo,descricao) values(1,'ROLE_CADASTRAR_CATEGORIA');
INSERT INTO permissao(codigo,descricao) values(2,'ROLE_PESQUISAR_CATEGORIA');
INSERT INTO permissao(codigo,descricao) values(3,'ROLE_CADASTRAR_PESSOA');
INSERT INTO permissao(codigo,descricao) values(4,'ROLE_REMOVER_PESSOA');
INSERT INTO permissao(codigo,descricao) values(5,'ROLE_PESQUISAR_PESSOA');
INSERT INTO permissao(codigo,descricao) values(6,'ROLE_CADASTRAR_LANCAMENTO');
INSERT INTO permissao(codigo,descricao) values(7,'ROLE_REMOVER_LANCAMENTO');
INSERT INTO permissao(codigo,descricao) values(8,'ROLE_PESQUISAR_LANCAMENTO');

INSERT INTO usuario_permissao(codigo_usuario,codigo_permissao) values(1,1);
INSERT INTO usuario_permissao(codigo_usuario,codigo_permissao) values(1,2);
INSERT INTO usuario_permissao(codigo_usuario,codigo_permissao) values(1,3);
INSERT INTO usuario_permissao(codigo_usuario,codigo_permissao) values(1,4);
INSERT INTO usuario_permissao(codigo_usuario,codigo_permissao) values(1,5);
INSERT INTO usuario_permissao(codigo_usuario,codigo_permissao) values(1,6);
INSERT INTO usuario_permissao(codigo_usuario,codigo_permissao) values(1,7);
INSERT INTO usuario_permissao(codigo_usuario,codigo_permissao) values(1,8);

INSERT INTO usuario_permissao(codigo_usuario,codigo_permissao) values(2,2);
INSERT INTO usuario_permissao(codigo_usuario,codigo_permissao) values(2,5);
INSERT INTO usuario_permissao(codigo_usuario,codigo_permissao) values(2,8);