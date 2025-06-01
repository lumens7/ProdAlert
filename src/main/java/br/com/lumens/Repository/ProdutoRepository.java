package br.com.lumens.Repository;

import br.com.lumens.DOMAIN.Produto;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/*
Criado por Luís
*/

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    // Busca produtos por nome e status
	@Query("SELECT p FROM Produto p WHERE p.nomeProduto LIKE %:nome% " +
	           "AND p.funcionario.CPF IN (SELECT v.CPF FROM Vincular v WHERE v.CNPJ = :cnpj) " +
	           "AND p.status = 'ATIVO'")
	    List<Produto> findByNomeAndEmpresa(
	        @Param("nome") String nome,
	        @Param("cnpj") String cnpj
	    );

    // Busca produtos por código de barras e status
	@Query("SELECT p FROM Produto p WHERE p.codBarras LIKE %:codigo% " +
	           "AND p.funcionario.CPF IN (SELECT v.CPF FROM Vincular v WHERE v.CNPJ = :cnpj) " +
	           "AND p.status = 'ATIVO'")
	    List<Produto> findByCodBarrasAndEmpresa(
	        @Param("codigo") String codigo,
	        @Param("cnpj") String cnpj
	    );
	// Busca produtos por um intervalo da data de contagem e status
	@Query("SELECT p FROM Produto p WHERE p.dataContagem BETWEEN :dataInicial AND :dataFinal " +
	           "AND p.funcionario.CPF IN (SELECT v.CPF FROM Vincular v WHERE v.CNPJ = :cnpj) " +
	           "AND p.status = 'ATIVO'")
	    List<Produto> findByDataContagemBetweenAndEmpresa(
	        @Param("dataInicial") LocalDate dataInicial,
	        @Param("dataFinal") LocalDate dataFinal,
	        @Param("cnpj") String cnpj
	    );

	// Busca por intervalo de data de validade, empresa e status
    @Query("SELECT p FROM Produto p WHERE p.dataValidade BETWEEN :dataInicial AND :dataFinal " +
           "AND p.funcionario.CPF IN (SELECT v.CPF FROM Vincular v WHERE v.CNPJ = :cnpj) " +
           "AND p.status = 'ATIVO'")
    List<Produto> findByDataValidadeBetweenAndEmpresa(
        @Param("dataInicial") LocalDate dataInicial,
        @Param("dataFinal") LocalDate dataFinal,
        @Param("cnpj") String cnpj
    );

 // Busca por nome e data de contagem, empresa e status
    @Query("SELECT p FROM Produto p WHERE p.nomeProduto LIKE %:nomeProduto% " +
    	       "AND p.dataContagem BETWEEN :dataInicial AND :dataFinal " +
    	       "AND p.funcionario.CPF IN (SELECT v.CPF FROM Vincular v WHERE v.CNPJ = :cnpj) " +
    	       "AND p.status = 'ATIVO'")
    	List<Produto> findByNomeProdutoAndDataContagemBetweenAndEmpresa(
    	    @Param("nomeProduto") String nomeProduto,
    	    @Param("dataInicial") LocalDate dataInicial,
    	    @Param("dataFinal") LocalDate dataFinal,
    	    @Param("cnpj") String cnpj
    	);

    // Busca por nome e data de validade, empresa e status
    @Query("SELECT p FROM Produto p WHERE p.nomeProduto LIKE %:nomeProduto% " +
    	       "AND p.dataValidade BETWEEN :dataInicial AND :dataFinal " +
    	       "AND p.funcionario.CPF IN (SELECT v.CPF FROM Vincular v WHERE v.CNPJ = :cnpj) " +
    	       "AND p.status = 'ATIVO'")
    	List<Produto> findByNomeProdutoAndDataValidadeBetweenAndEmpresa(
    	    @Param("nomeProduto") String nomeProduto,
    	    @Param("dataInicial") LocalDate dataInicial,
    	    @Param("dataFinal") LocalDate dataFinal,
    	    @Param("cnpj") String cnpj
    	);

 // Busca todos os produtos ativos da empresa
    @Query("SELECT p FROM Produto p WHERE " +
           "p.funcionario.CPF IN (SELECT v.CPF FROM Vincular v WHERE v.CNPJ = :cnpj) " +
           "AND p.status = 'ATIVO'")
    List<Produto> findAllByEmpresa(@Param("cnpj") String cnpj);


    //Busca os ultimos 6 produtos cadastrados no sistema, essa parte serve para ficar no front.
    @Query("SELECT p FROM Produto p WHERE p.funcionario.CPF IN (SELECT v.CPF FROM Vincular v WHERE v.CNPJ = :cnpjEmpresa) " +
            "ORDER BY p.dataContagem DESC")
     List<Produto> findTop6ByEmpresaOrderByDataContagemDesc(@Param("cnpjEmpresa") String cnpjEmpresa, Pageable pageable);

    // Busca produtos com datas de validade próximas
    @Query("SELECT p FROM Produto p WHERE p.dataValidade IN :datas")
    List<Produto> findByDataValidadeIn(@Param("datas") List<LocalDate> datas);

    // Inativa produtos vencidos
    @Modifying
    @Transactional
    @Query("UPDATE Produto p SET p.status = 'INATIVO' WHERE p.dataValidade < CURRENT_DATE")
    void inativarProdutosVencidos();
}
