package br.com.lumens.Controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import br.com.lumens.DOMAIN.Produto;
import br.com.lumens.DOMAIN.Role;
import br.com.lumens.DOMAIN.User;
import br.com.lumens.DOMAIN.Vincular;
import br.com.lumens.DTO.AlterRequest;
import br.com.lumens.DTO.CompleteSignupRequest;
import br.com.lumens.DTO.ConfirmacaoSenhaRequest;
import br.com.lumens.DTO.LoginRequest;
import br.com.lumens.DTO.SignupRequest;
import br.com.lumens.Repository.RoleRepository;
import br.com.lumens.Repository.UserRepository;
import br.com.lumens.Repository.VincularRepository;
import br.com.lumens.Service.EmailService;
import br.com.lumens.Service.EmailVerificationService;
import br.com.lumens.Service.ProdutoService;
import br.com.lumens.Service.UserService;
import br.com.lumens.Service.VincularService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;

/*
Criado por Luís
*/

@RestController
@RequestMapping("/api/usuario")
public class UserController {

	@Autowired
    private UserService userService;
    
    private final UserRepository userRepository;
    
    private final RoleRepository roleRepository;
    
    private final VincularService vincularService;
    
    private final VincularRepository vincularRepository;
    
    private final EmailService emailService;
    
    private final EmailVerificationService emailVerificationService;
    
    @Autowired
    private final CompleteSignupRequest completeSignupRequest;
    
    private AuthenticationManager authenticationManager;

    @Value("${JWT_SECRET:#{null}}")
    private String secretKey;
    @Autowired
    public UserController(RoleRepository roleRepository, VincularRepository vincularRepository, UserRepository userRepository, EmailVerificationService emailVerificationService, EmailService emailService, CompleteSignupRequest completeSignupRequest, VincularService vincularService, AuthenticationManager authenticationManager) {
    	this.roleRepository = roleRepository;
    	this.vincularRepository = vincularRepository;
    	this.userRepository = userRepository;
    	this.emailVerificationService = emailVerificationService;
    	this.emailService = emailService;
    	this.completeSignupRequest = completeSignupRequest;
    	this.vincularService = vincularService;
    	this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getMail(), loginRequest.getSenha())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

            String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles) 
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 14400000))
                .signWith(SignatureAlgorithm.HS512, secretKey.getBytes()) 
                .compact();

            Map<String, Object> response = new HashMap();
            response.put("token", "Bearer " + token);
            response.put("username", userDetails.getUsername());
            response.put("roles", roles);

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("E-mail ou senha incorretos");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Acesso negado. Contate o administrador.");
        }
    }

    @PostMapping("/init-signup")
    public ResponseEntity<?> iniciarCadastro(@Valid @RequestBody SignupRequest signupRequest) {
        try {
        	
        	String senha = signupRequest.getSenha();
        	String confirmarSenha = signupRequest.getConfirmarSenha();
        	if(senha.length() != 8 || confirmarSenha.length() != 8) {
        		return ResponseEntity.badRequest().body("Senha informada no formato invalido.\nSenha deve Conter 8 digitos!");
        	}
        	if(!confirmarSenha.equals(senha)) {
        		return ResponseEntity.badRequest().body("Senhas informadas estão diferentes!\nFavor insira senhas iguais.");
        	}
            if (signupRequest.getMail() == null || !signupRequest.getMail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                return ResponseEntity.badRequest().body("Por favor, forneça um endereço de e-mail válido.");
            }
            if (userRepository.existsByMail(signupRequest.getMail())) {
                return ResponseEntity.badRequest().body("Este e-mail já está cadastrado!");
            }
            userService.salvarDadosTemporarios(signupRequest);
            String verificationCode = emailVerificationService.generateVerificationCode(signupRequest.getMail());
            String mensagemCadastro = String.format("""
            	    <!DOCTYPE html>
            	    <html>
            	    <head>
            	        <style>
            	            body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
            	            .header { background-color: #f0f7ff; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
            	            .code { font-size: 24px; letter-spacing: 3px; text-align: center; margin: 25px 0; padding: 15px; background-color: #e8f4ff; border-radius: 5px; }
            	            .footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }
            	            .alert { background-color: #fff3cd; padding: 10px; border-radius: 5px; margin: 15px 0; }
            	        </style>
            	    </head>
            	    <body>
            	        <div class="header">
            	            <h3 style="color: #2e59a7;">Validação de Cadastro LUMENS</h3>
            	        </div>
            	        
            	        <div class="content">
            	            <p>Olá,</p>
            	            
            	            <p>Para completar seu cadastro na plataforma LUMENS, utilize o seguinte código de verificação:</p>
            	            
            	            <div class="code">
            	                ✉️ <strong>%s</strong>
            	            </div>
            	            
            	            <p style="text-align: center;">Este código expirará em <strong>5 minutos</strong></p>
            	            
            	            <div class="alert">
            	                <p><strong>Importante:</strong> Nunca compartilhe códigos de verificação com terceiros.</p>
            	            </div>
            	            
            	            <p>Se você não solicitou este código, por favor ignore este e-mail ou entre em contato com nosso suporte.</p>
            	        </div>
            	        
            	        <div class="footer">
            	            <p>Atenciosamente,</p>
            	            <p>Equipe LUMENS LTDA</p>
            	            <p>© %d LUMENS LTDA. Todos os direitos reservados.</p>
            	        </div>
            	    </body>
            	    </html>
            	""",
            	verificationCode,
            	LocalDate.now().getYear());

            	emailService.enviarEmailHtml(
            	    signupRequest.getMail(),
            	    "✉️ Seu Código de Verificação LUMENS",
            	    mensagemCadastro
            	);

            return ResponseEntity.ok().body("Código de verificação enviado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao iniciar cadastro: " + e.getMessage());
        }
    }

    @PostMapping("/complete-signup")
    public ResponseEntity<?> completarCadastro(@Valid @RequestBody CompleteSignupRequest request) {
        try {
            if (!emailVerificationService.verifyCode(request.getEmail(), request.getCode())) {
                return ResponseEntity.badRequest().body("Código inválido ou expirado.");
            }
            SignupRequest signupRequest = userService.recuperarDadosTemporarios(request.getEmail());
            if (signupRequest == null) {
                return ResponseEntity.badRequest().body("Dados de cadastro não encontrados ou expirados.");
            }
            User user = userService.cadastrarUser(signupRequest);
            
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao completar cadastro: " + e.getMessage());
        }
    }
    
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestParam String email, @RequestParam String code) {
        try {
            boolean isValid = emailVerificationService.verifyCode(email, code);
            
            if (isValid) {
                return ResponseEntity.ok().body("E-mail verificado com sucesso!");
            } else {
                return ResponseEntity.badRequest().body("Código inválido ou expirado.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao verificar código: " + e.getMessage());
        }
    }
    
    @GetMapping("/buscar/Ativos")
    public ResponseEntity<?> buscarFuncionariosDaEmpresa() {
        try {
            String emailEmpresa = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> empresaOpt = userService.buscarPorMail(emailEmpresa);
            if (!empresaOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empresa não encontrada");
            }
            User empresa = empresaOpt.get();
            if (empresa.getFunction() != User.functionRole.EMPRESA) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas empresas podem visualizar funcionários");
            }
            if (empresa.getCNPJ() == null) {
                return ResponseEntity.badRequest().body("Empresa não possui CNPJ cadastrado");
            }
            List<User> funcionarios = userService.buscarFuncionariosDaEmpresa(empresa.getCNPJ());
            return ResponseEntity.ok(funcionarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao buscar funcionários: " + e.getMessage());
        }
    }
    
    @GetMapping("/buscar/nomeUsuario")
    public ResponseEntity<?> buscarPorNomeUsuario(@RequestParam("nomeUser") String nomeUser) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> usuarioOpt = userService.buscarPorMail(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
            }
            User usuario = usuarioOpt.get();
            String cnpjEmpresa = null;
            if (usuario.getFunction() == User.functionRole.FUNCIONARIO) {
                Optional<Vincular> vinculo = vincularRepository.findByCPF(usuario.getCPF())
                    .stream()
                    .findFirst();
                if (vinculo.isPresent()) {
                    cnpjEmpresa = vinculo.get().getCNPJ();
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Funcionário não vinculado a uma empresa");
                }
            } 
            else if (usuario.getFunction() == User.functionRole.EMPRESA) {
                cnpjEmpresa = usuario.getCNPJ();
            }

            if (cnpjEmpresa == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Não foi possível identificar a empresa");
            }
            List<User> usuarios = userService.buscarPorNomeUsuario(nomeUser, cnpjEmpresa);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao buscar usuários: " + e.getMessage());
        }
    }
    
    @GetMapping("/buscar/CPF")
    public ResponseEntity<?> buscarPorCPFUsuario(@RequestParam("CPF") String CPF) {
        try {
            User userPesquisaPorCPF = userService.buscarPorCPFUsuario(CPF);
            if (userPesquisaPorCPF == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
            }
            return ResponseEntity.ok(userPesquisaPorCPF);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro inesperado ao pesquisar usuário pelo CPF.");
        }
    }
    
    @GetMapping("/buscarPorCPF")
    public ResponseEntity<?> buscarFuncionarioPorCPF(@RequestParam String CPF) {
        try {
            Map<String, Object> response = userService.buscarFuncionarioParaVinculo(CPF);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/vincular")
    public ResponseEntity<?> vincularFuncionarioEmpresa(@RequestParam String cpf) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User empresa = userService.buscarPorMail(email)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada!"));
            if (empresa.getFunction() != User.functionRole.EMPRESA) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Apenas empresas podem vincular funcionários!");
            }
            userService.vincularFuncionarioEmpresa(cpf, empresa.getCNPJ());
            return ResponseEntity.ok("Funcionário vinculado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/ativarFuncionario")
    public ResponseEntity<?> ativarFuncionario(@RequestParam String CPF) {
        try {
            userService.ativarUsuario(CPF);
            return ResponseEntity.ok("Funcionário ativado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/inativar")
    public ResponseEntity<?> inativarFuncionario(@RequestParam("CPF") String CPF) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String emailEmpresa = authentication.getName(); 
            Optional<User> empresaOpt = userService.buscarPorMail(emailEmpresa);
            if (!empresaOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empresa não encontrada");
            }
            User empresa = empresaOpt.get();
            if (empresa.getFunction() != User.functionRole.EMPRESA) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas empresas podem inativar funcionários");
            }
            boolean vinculado = vincularService.verificarVinculacao(empresa.getCNPJ(), CPF);
            if (!vinculado) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Este funcionário não pertence à sua empresa!");
            }
            User funcionario = userService.buscarPorCPFUsuario(CPF);
            if (funcionario == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Funcionário não encontrado ou já inativado!");
            }
            userService.inativarFuncionario(funcionario, empresa);
            return ResponseEntity.ok("Funcionário " + funcionario.getNomeUser() + " foi inativado com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Erro inesperado ao inativar funcionário.");
        }
    }
    
    @GetMapping("/buscar/email")
    public ResponseEntity<?> buscarPorEmail(@RequestParam("email") String email) {
        try {
            Optional<User> user = userService.buscarPorMail(email);
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Nenhum usuário encontrado com o email informado."));
            }
            User usuario = user.get();
            Map<String, String> response = new HashMap<>();
            response.put("nomeUser", usuario.getNomeUser());
            if (usuario.getFunction() == User.functionRole.FUNCIONARIO && usuario.getCPF() != null) {
                Optional<Vincular> vinculo = vincularRepository.findByCPF(usuario.getCPF())
                    .stream()
                    .findFirst();
                String nomeEmpresa = vinculo
                    .map(v -> userService.buscarPorCNPJ(v.getCNPJ()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(User::getNomeUser)
                    .orElse("N/A");
                response.put("empresa", nomeEmpresa);
            } else {
                response.put("empresa", "N/A");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro ao buscar usuário: " + e.getMessage()));
        }
    }
    
    @GetMapping("/dados_usuario")
    public ResponseEntity<?> getDadosUsuario() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOpt = userService.buscarPorMail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Nenhum usuário encontrado"));
            }
            User user = userOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("nomeUser", user.getNomeUser());
            response.put("mail", user.getMail());
            response.put("CPF", user.getCPF());
            response.put("CNPJ", user.getCNPJ());
            response.put("function", user.getFunction());
            response.put("statusUser", user.getStatusUser());
            if (user.getFunction() == User.functionRole.FUNCIONARIO && user.getCPF() != null) {
                Optional<User> empresa = vincularRepository.findByCPF(user.getCPF())
                    .stream()
                    .findFirst()
                    .map(v -> userService.buscarPorCNPJ(v.getCNPJ()))
                    .flatMap(Function.identity());
                if (empresa.isPresent()) {
                    Map<String, String> empresaData = new HashMap<>();
                    empresaData.put("nomeUser", empresa.get().getNomeUser());
                    empresaData.put("CNPJ", empresa.get().getCNPJ());
                    response.put("empresa", empresaData);
                } else {
                    response.put("empresa", null);
                }
            } else {
                response.put("empresa", null);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro ao buscar dados: " + e.getMessage()));
        }
    }
    
    @PostMapping("/alterarSenha")
    public ResponseEntity<?> alterarDadosUser(@RequestBody AlterRequest alterRequest) {
        try {
            if (alterRequest.getMail() == null || alterRequest.getMail().isBlank()) {
                return ResponseEntity.badRequest().body("E-mail é obrigatório");
            }
            return userService.alterarDadosUser(alterRequest);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao processar solicitação: " + e.getMessage());
        }
    }
    
    @PostMapping("/confirmarAlteracaoSenha")
    public ResponseEntity<?> confirmarAlteracaoSenha(@RequestBody ConfirmacaoSenhaRequest request) {
        return userService.confirmarAlteracaoSenha(request);
    }
}