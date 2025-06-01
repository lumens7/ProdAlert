package br.com.lumens.Repository;

import br.com.lumens.DOMAIN.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
Criado por Luís
*/

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    boolean existsByMail(String mail);

    @Query("SELECT u FROM User u WHERE u.mail = :mail")
    Optional<User> findByMail(@Param("mail") String mail);

    @Query("SELECT u FROM User u WHERE u.mail LIKE %:mail% AND u.statusUser = :statusUser")
    Optional<User> findByMailStartingWithAndStatusUser(
        @Param("mail") String mail, 
        @Param("statusUser") User.StatusUser statusUser
    );

    @Query("SELECT u FROM User u WHERE u.statusUser = :statusUser")
    List<User> findByStatusUser(@Param("statusUser") User.StatusUser statusUser);

    // Métodos atualizados para usar CPF/CNPJ em vez de ID
    @Query("SELECT u FROM User u WHERE u.CPF = :cpf")
    Optional<User> findByCPF(@Param("cpf") String cpf);

    @Query("SELECT u FROM User u WHERE u.CNPJ = :cnpj")
    Optional<User> findByCNPJ(@Param("cnpj") String cnpj);

    // Busca usuários por nome e status (sem relação com empresa)
    @Query("SELECT u FROM User u WHERE u.nomeUser LIKE %:nomeUser% AND u.statusUser = :statusUser")
    List<User> findByNomeUserStartingWithAndStatusUser(
        @Param("nomeUser") String nomeUser,
        @Param("statusUser") User.StatusUser statusUser
    );

    // Busca usuário por CPF e status
    @Query("SELECT u FROM User u WHERE u.CPF = :CPF AND u.statusUser = :statusUser")
    User findByCPFAndStatusUser(
        @Param("CPF") String CPF, 
        @Param("statusUser") User.StatusUser statusUser
    );

    // Busca funcionários vinculados a uma empresa (via CNPJ)
    @Query("SELECT u FROM User u JOIN Vincular v ON u.CPF = v.CPF WHERE v.CNPJ = :cnpj AND u.statusUser = :statusUser")
    List<User> findFuncionariosByEmpresaCnpj(
        @Param("cnpj") String cnpj,
        @Param("statusUser") User.StatusUser statusUser
    );

    // Busca funcionários por nome e empresa (via CNPJ)
    @Query("SELECT u FROM User u JOIN Vincular v ON u.CPF = v.CPF WHERE v.CNPJ = :cnpj AND u.nomeUser LIKE %:nome% AND u.statusUser = :statusUser")
    List<User> findByNomeAndEmpresaCnpj(
        @Param("nome") String nome,
        @Param("cnpj") String cnpj,
        @Param("statusUser") User.StatusUser statusUser
    );
}