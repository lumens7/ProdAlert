package br.com.lumens.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.lumens.DOMAIN.Role;
import br.com.lumens.Service.RoleService;
import jakarta.validation.Valid;

/*
Criado por Luís
*/

@RestController
@RequestMapping("/api/role")
public class RoleController {
	
	private RoleService roleService;

	@PostMapping("/cadastro")
    public ResponseEntity<?> cadastroRole(@Valid @RequestBody Role role) {
		Role rolecadastrada = roleService.cadastroRole(role);
		return ResponseEntity.ok(rolecadastrada);
	}
	
	@GetMapping("/buscar")
	public ResponseEntity<?> buscarTodos() {
		List<Role> roles = roleService.buscarRoles();
		return ResponseEntity.ok(roles);
	}
}
