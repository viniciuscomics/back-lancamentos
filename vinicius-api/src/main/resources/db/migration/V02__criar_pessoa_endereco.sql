CREATE TABLE pessoa(
id BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
nome VARCHAR(30) NOT NULL,
logradouro VARCHAR(40) NOT NULL,
numero VARCHAR(5) NOT NULL,
complemento VARCHAR(10) NOT NULL,
bairro VARCHAR(20) NOT NULL,
cep VARCHAR(9) NOT NULL,
cidade VARCHAR(20) NOT NULL,
estado VARCHAR(15) NOT NULL,
ativo bit NOT NULL

)ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO pessoa(nome,logradouro,numero,complemento,bairro,cep,cidade,estado,ativo)values('Vinicius Costa','Aurea E.Pereira Valadao','12','cs 4','Jd Guanhembu','04814610','Sao Paulo','SP',true);
INSERT INTO pessoa(nome,logradouro,numero,complemento,bairro,cep,cidade,estado,ativo)values('Aline Costa','Aurea E.Pereira Valadao','12','cs 4','Jd Guanhembu','04814610','Sao Paulo','SP',true);
INSERT INTO pessoa(nome,logradouro,numero,complemento,bairro,cep,cidade,estado,ativo)values('Gustavo Costa','Aurea E.Pereira Valadao','12','cs 4','Jd Guanhembu','04814610','Sao Paulo','SP',true);
