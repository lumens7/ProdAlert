package br.com.lumens.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.lumens.DOMAIN.Role;

/*
Criado por Luís
*/

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer>{
	@Query("SELECT r FROM Role r WHERE r.nomeRole = :nomeRole")
	Optional<Role> findByNomeRole(@Param("nomeRole") String nomeRole);
}