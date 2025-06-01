-- Tabela: produto
CREATE TABLE produto (
    id_produto SERIAL PRIMARY KEY,
    nome_produto VARCHAR(255) NOT NULL,
    descricao_produto VARCHAR(255) NOT NULL,
    cod_barras VARCHAR(13),
    data_contagem VARCHAR(255),
    data_validade VARCHAR(255) NOT NULL,
    quantidade_contada DOUBLE PRECISION NOT NULL,
    status_produto VARCHAR(255)
);

-- Tabela: role
CREATE TABLE role (
    id_role SERIAL PRIMARY KEY,
    nome_role VARCHAR(255) NOT NULL
);

-- Tabela: tbuser (usuário)
CREATE TABLE tbuser (
    id_user SERIAL PRIMARY KEY,
    nome_user VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) UNIQUE,
    cnpj VARCHAR(14) UNIQUE,
    mail VARCHAR(255) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    status_user VARCHAR(255),
    function_role VARCHAR(255),
    id_empresa INTEGER
);

-- Tabela: usuario_role (relacionamento ManyToMany entre User e Role)
CREATE TABLE usuario_role (
    id_user INTEGER NOT NULL,
    id_role INTEGER NOT NULL,
    PRIMARY KEY (id_user, id_role),
    FOREIGN KEY (id_user) REFERENCES tbuser(id_user),
    FOREIGN KEY (id_role) REFERENCES role(id_role)
);

-- Adicionando chave estrangeira para o relacionamento ManyToOne (User -> Empresa)
ALTER TABLE tbuser
ADD CONSTRAINT fk_empresa
FOREIGN KEY (id_empresa) REFERENCES tbuser(id_user);