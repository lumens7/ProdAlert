package br.com.lumens.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.lumens.DOMAIN.Role;
import br.com.lumens.Repository.RoleRepository;

/*
Criado por Luís
*/

@Service
public class RoleService {
	
	private final RoleRepository roleRepository;
	
	public RoleService(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}
	
	public Role cadastroRole(Role role) {
		return roleRepository.save(role);
	}
	
	public List<Role> buscarRoles(){
		return roleRepository.findAll();
	}
}
