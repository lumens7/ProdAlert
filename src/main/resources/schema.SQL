/* Tabela Role */
CREATE TABLE `tbrole` (
  `id_Role` int NOT NULL AUTO_INCREMENT,
  `nome_Role` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_Role`)
);

/* Tabela Usuario */
CREATE TABLE `tbuser` (
  `id_User` int NOT NULL AUTO_INCREMENT,
  `nome_User` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `CPF` char(11) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CNPJ` char(14) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mail` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `senha` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status_User` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `funcao` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id_User`),
  UNIQUE KEY `unique_mail` (`mail`),
  UNIQUE KEY `CPF` (`CPF`),
  UNIQUE KEY `CNPJ` (`CNPJ`),
  CONSTRAINT `tbuser_chk_1` CHECK ((`status_User` in (_utf8mb4'ATIVO',_utf8mb4'INATIVO'))),
  CONSTRAINT `tbuser_chk_2` CHECK ((`funcao` in (_utf8mb4'EMPRESA',_utf8mb4'FUNCIONARIO')))
);

/* Tabela Produto */
CREATE TABLE `tbproduto` (
  `id_Produto` int NOT NULL AUTO_INCREMENT,
  `nome_Produto` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descricao_Produto` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cod_barras` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data_Contagem` date DEFAULT NULL,
  `data_Validade` date NOT NULL,
  `quantidade_Contada` decimal(10,2) NOT NULL,
  `status_Produto` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `id_Funcionario` int NOT NULL,
  PRIMARY KEY (`id_Produto`),
  KEY `id_Funcionario` (`id_Funcionario`),
  CONSTRAINT `tbproduto_ibfk_1` FOREIGN KEY (`id_Funcionario`) REFERENCES `tbuser` (`id_User`) ON DELETE CASCADE,
  CONSTRAINT `tbproduto_chk_1` CHECK ((`status_Produto` in (_utf8mb4'ATIVO',_utf8mb4'INATIVO')))
);

/* Tabela para relacionamento da tabela Usuario com a tabela Role */
CREATE TABLE `usuario_role` (
  `id_User` int NOT NULL,
  `id_Role` int NOT NULL,
  PRIMARY KEY (`id_User`,`id_Role`),
  KEY `id_Role` (`id_Role`),
  CONSTRAINT `usuario_role_ibfk_1` FOREIGN KEY (`id_User`) REFERENCES `tbuser` (`id_User`) ON DELETE CASCADE,
  CONSTRAINT `usuario_role_ibfk_2` FOREIGN KEY (`id_Role`) REFERENCES `tbrole` (`id_Role`) ON DELETE CASCADE
) ;


/*Tabela para relacionamento entre empresas e funcionarios*/
CREATE TABLE `tbvincular` (
  `CNPJ` char(14) COLLATE utf8mb4_unicode_ci NOT NULL,
  `CPF` char(11) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`CNPJ`,`CPF`),
  KEY `tbvincular_ibfk_2` (`CPF`),
  CONSTRAINT `tbvincular_ibfk_1` FOREIGN KEY (`CNPJ`) REFERENCES `tbuser` (`CNPJ`) ON DELETE CASCADE,
  CONSTRAINT `tbvincular_ibfk_2` FOREIGN KEY (`CPF`) REFERENCES `tbuser` (`CPF`) ON DELETE CASCADE
);