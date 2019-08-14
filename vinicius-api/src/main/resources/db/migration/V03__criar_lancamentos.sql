CREATE TABLE IF NOT EXISTS lancamento(
id BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
descricao VARCHAR(50) NOT NULL,
data_vencimento DATE NOT NULL,
data_pagamento DATE,
valor DECIMAL(10,2) NOT NULL,
observacao VARCHAR(100),
tipo VARCHAR(20) NOT NULL,
id_categoria BIGINT(20) NOT NULL,
id_pessoa BIGINT(20) NOT NULL,
FOREIGN KEY (id_categoria) REFERENCES categoria(id),
FOREIGN KEY (id_pessoa) REFERENCES pessoa(id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO lancamento(descricao,data_vencimento,data_pagamento, valor, observacao, tipo, id_categoria, id_pessoa)values('Salario Mensal', '2017-07-10',null,5000.00,'Distribuição','RECEITA',2,2);
INSERT INTO lancamento(descricao,data_vencimento,data_pagamento, valor, observacao, tipo, id_categoria, id_pessoa)values('Salario Mensal', '2017-08-10',null,5000.00,'Distribuição','RECEITA',3,3);