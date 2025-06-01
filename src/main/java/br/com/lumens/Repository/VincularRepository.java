package br.com.lumens.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.lumens.DOMAIN.User;
import br.com.lumens.DOMAIN.Vincular;
import br.com.lumens.DOMAIN.VincularId;
import jakarta.transaction.Transactional;

/*
Criado por Luís
*/

@Repository
public interface VincularRepository extends JpaRepository<Vincular, VincularId> {
    
    // Método corrigido para verificar existência de vínculo
	boolean existsByCNPJAndCPF(String cnpj, String cpf);

    // Método para buscar todos os vínculos por CNPJ
    @Query("SELECT v FROM Vincular v WHERE v.CNPJ = :cnpj")
    List<Vincular> findByCNPJ(@Param("cnpj") String cnpj);
    
    @Query("SELECT v FROM Vincular v WHERE v.CPF = :cpf")
    List<Vincular> findByCPF(@Param("cpf") String cpf);

    // Método para buscar o User funcionário através do vínculo (opcional)
    @Query("SELECT u FROM User u JOIN Vincular v ON u.CPF = v.CPF WHERE v.CNPJ = :cnpj")
    List<User> findFuncionariosByEmpresaCnpj(@Param("cnpj") String cnpj);
    
    //metodo para desvincular um usuario com ROLE funcionario de um usuario com ROLE empresa
    @Modifying
    @Transactional
    @Query("DELETE FROM Vincular v WHERE v.id.CPF = :cpf AND v.id.CNPJ = :cnpj")
    void deleteByCpfAndCnpj(@Param("cpf") String cpf, @Param("cnpj") String cnpj);


}