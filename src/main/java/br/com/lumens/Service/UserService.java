package br.com.lumens.Service;

import br.com.lumens.Controller.ProdutoController;
import br.com.lumens.DOMAIN.Role;
import br.com.lumens.DOMAIN.User;
import br.com.lumens.DOMAIN.User.StatusUser;
import br.com.lumens.DOMAIN.Vincular;
import br.com.lumens.DTO.AlterRequest;
import br.com.lumens.DTO.ConfirmacaoSenhaRequest;
import br.com.lumens.DTO.SignupRequest;
import br.com.lumens.Repository.RoleRepository;
import br.com.lumens.Repository.UserRepository;
import br.com.lumens.Repository.VincularRepository;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/*
Criado por Luís
*/

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VincularRepository vincularRepository;
    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;
    private static final String TEMP_SIGNUP_CACHE = "tempSignupData";

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, VincularRepository vincularRepository, EmailVerificationService emailVerificationService, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.vincularRepository = vincularRepository;
        this.emailVerificationService = emailVerificationService;
        this.emailService = emailService;
    }

    public User cadastrarUser(SignupRequest signupRequest) {
        if (signupRequest.getSenha().length() < 8) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres!");
        }
        User user = new User();
        user.setNomeUser(signupRequest.getNomeUser());
        user.setMail(signupRequest.getMail());
        user.setSenha(passwordEncoder.encode(signupRequest.getSenha()));
        user.setStatusUser(User.StatusUser.ATIVO);

        String cpfcnpj = signupRequest.getCpfcnpj().trim();
        Set<Role> roles = new HashSet<>();
        if (userRepository.existsByMail(user.getMail())) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado!");
        }
        if (cpfcnpj == null) {
            throw new IllegalArgumentException("O campo CPF ou CNPJ deve ser informado!");
        }
        if (cpfcnpj.length() == 11) {
            if (!cpfcnpj.matches("\\d+")) {
                throw new IllegalArgumentException("CPF deve conter apenas números!");
            }
            user.setCPF(cpfcnpj);
            user.setFunction(User.functionRole.FUNCIONARIO);
            roles.add(roleRepository.findByNomeRole("FUNCIONARIO")
                .orElseThrow(() -> new RuntimeException("Role FUNCIONARIO não encontrada!")));
        } else {
            if (!cpfcnpj.matches("\\d+")) {
                throw new IllegalArgumentException("CNPJ deve conter apenas números!");
            }
            user.setCNPJ(cpfcnpj);
            user.setFunction(User.functionRole.EMPRESA);
            roles.add(roleRepository.findByNomeRole("EMPRESA")
                .orElseThrow(() -> new RuntimeException("Role EMPRESA não encontrada!")));
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }
    
    @CachePut(value = TEMP_SIGNUP_CACHE, key = "#signupRequest.mail")
    public SignupRequest salvarDadosTemporarios(SignupRequest signupRequest) {
        return signupRequest;
    }
    
    @Cacheable(value = TEMP_SIGNUP_CACHE, key = "#email")
    public SignupRequest recuperarDadosTemporarios(String email) {
        return null;
    }
    
    public Optional<User> buscarPorMail(String mail) {
        return userRepository.findByMailStartingWithAndStatusUser(mail, User.StatusUser.ATIVO);
    }
    
    public List<User> buscarTodosUser() {
        return userRepository.findByStatusUser(User.StatusUser.ATIVO);
    }

    public List<User> buscarFuncionariosDaEmpresa(String cnpjEmpresa) {
    	 return vincularRepository.findFuncionariosByEmpresaCnpj(cnpjEmpresa);
    }

    public List<User> buscarPorNomeUsuario(String nomeUser, String cnpjEmpresa) {
        return userRepository.findByNomeAndEmpresaCnpj(
            nomeUser, 
            cnpjEmpresa, 
            User.StatusUser.ATIVO
        );
    }

    public User buscarPorCPFUsuario(String CPF) {
        return userRepository.findByCPFAndStatusUser(CPF, User.StatusUser.ATIVO);
    }
    public Optional<User> buscarPorCNPJ(String cnpj) {
        return userRepository.findByCNPJ(cnpj);
    }

    public Map<String, Object> buscarFuncionarioParaVinculo(String CPF) {
        User funcionario = userRepository.findByCPF(CPF)
            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
        
        if (funcionario.getFunction() != User.functionRole.FUNCIONARIO) {
            throw new IllegalArgumentException("O usuário não é um funcionário");
        }
        Optional<Vincular> vinculoExistente = vincularRepository.findByCPF(funcionario.getCPF()).stream().findFirst();
        if (vinculoExistente.isPresent()) {
            User empresaVinculada = userRepository.findByCNPJ(vinculoExistente.get().getCNPJ())
                .orElseThrow(() -> new IllegalArgumentException("Empresa vinculada não encontrada"));
            
            throw new IllegalArgumentException(
                String.format("Funcionário já vinculado à empresa: %s (CNPJ: %s)", 
                    empresaVinculada.getNomeUser(), 
                    formatarCNPJ(empresaVinculada.getCNPJ()))
            );
        }
        Map<String, Object> response = new HashMap<>();
        response.put("nomeUser", funcionario.getNomeUser());
        response.put("CPF", funcionario.getCPF());
        response.put("mail", funcionario.getMail());
        response.put("status", funcionario.getStatusUser().toString());
        return response;
    }
    
    private String formatarCNPJ(String cnpj) {
        return cnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    @Transactional
    public void vincularFuncionarioEmpresa(String cpfFuncionario, String cnpjEmpresa) {
        User funcionario = userRepository.findByCPF(cpfFuncionario)
            .orElseThrow(() -> new IllegalArgumentException("Funcionário ativo não encontrado!"));
        User empresa = userRepository.findByCNPJ(cnpjEmpresa)
            .orElseThrow(() -> new IllegalArgumentException("Empresa ativa não encontrada!"));
        if (funcionario.getFunction() != User.functionRole.FUNCIONARIO) {
            throw new IllegalArgumentException("O usuário não é um funcionário!");
        }
        if (empresa.getFunction() != User.functionRole.EMPRESA) {
            throw new IllegalArgumentException("O usuário não é uma empresa!");
        }
        if (vincularRepository.existsByCNPJAndCPF(cnpjEmpresa, cpfFuncionario)) {
            throw new IllegalArgumentException("Funcionário já vinculado a esta empresa!");
        }
        String mensagemVinculo = String.format("""
        	    <!DOCTYPE html>
        	    <html>
        	    <head>
        	        <style>
        	            body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
        	            .header { background-color: #f0f7ff; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
        	            .content { padding: 20px; }
        	            .info-box { background-color: #f8f9fa; border-left: 4px solid #4e73df; padding: 15px; margin: 15px 0; }
        	            .footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }
        	            .button { background-color: #4e73df; color: white; padding: 10px 15px; text-decoration: none; border-radius: 4px; display: inline-block; }
        	        </style>
        	    </head>
        	    <body>
        	        <div class="header">
        	            <h2 style="color: #2e59a7;">Bem-vindo(a) à Plataforma LUMENS</h2>
        	        </div>
        	        
        	        <div class="content">
        	            <p>Caro(a) <strong>%s</strong>,</p>
        	            
        	            <div class="info-box">
        	                <p>Confirmamos o estabelecimento do seu vínculo profissional com:</p>
        	                <p><strong>%s</strong> (CNPJ: %s)</p>
        	                <p>Data do vínculo: <strong>%s</strong></p>
        	            </div>
        	            
        	            <p>A partir de agora, você tem acesso aos recursos da plataforma conforme as permissões definidas pela empresa.</p>
        	            
        	        </div>
        	        
        	        <div class="footer">
        	            <p>Este e-mail foi enviado automaticamente pela plataforma LUMENS.</p>
        	            <p>© %d LUMENS LTDA. Todos os direitos reservados.</p>
        	            <p style="font-size: 11px; color: #999;">
        	                Se você recebeu esta mensagem por engano, por favor ignore ou notifique: suporte@lumens.com.br
        	            </p>
        	        </div>
        	    </body>
        	    </html>
        	""", 
        	funcionario.getNomeUser(),
        	empresa.getNomeUser(),
        	formatCNPJ(empresa.getCNPJ()),
        	LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
        	LocalDate.now().getYear());
        	emailService.enviarEmailHtml(
        	    funcionario.getMail(),
        	    "🟢 Confirmação de Vínculo com " + empresa.getNomeUser(),
        	    mensagemVinculo
        	);
        Vincular vinculo = new Vincular();
        vinculo.setCNPJ(cnpjEmpresa);
        vinculo.setCPF(cpfFuncionario);
        vincularRepository.save(vinculo);
    }
    
    @Transactional
    public void ativarUsuario(String CPF) {
        User usuario = userRepository.findByCPF(CPF)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        
        if (usuario.getStatusUser() == User.StatusUser.ATIVO) {
            throw new IllegalArgumentException("Usuário já está ativo");
        }
        
        usuario.setStatusUser(User.StatusUser.ATIVO);
        userRepository.save(usuario);
    }
    
    private String formatCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return cnpj;
        }
        return cnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    public void inativarFuncionario(User funcionario, User empresa) {
    	vincularRepository.deleteByCpfAndCnpj(funcionario.getCPF(), empresa.getCNPJ());
        funcionario.setStatusUser(StatusUser.INATIVO);
        userRepository.save(funcionario);
        String mensagemDesligamento = String.format("""
        	    <!DOCTYPE html>
        	    <html>
        	    <head>
        	        <style>
        	            body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
        	            .header { background-color: #fff3f3; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
        	            .content { padding: 20px; }
        	            .info-box { background-color: #f8f9fa; border-left: 4px solid #e74a3b; padding: 15px; margin: 15px 0; }
        	            .footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }
        	            .signature { margin-top: 30px; }
        	        </style>
        	    </head>
        	    <body>
        	        <div class="header">
        	            <h2 style="color: #e74a3b;">Encerramento de Vínculo</h2>
        	        </div>
        	        
        	        <div class="content">
        	            <p>Prezado(a) <strong>%s</strong>,</p>
        	            
        	            <div class="info-box">
        	                <p>Comunicamos que seu vínculo com <strong>%s</strong> foi encerrado em <strong>%s</strong>.</p>
        	            </div>
        	            
        	            <p>Gostaríamos de expressar nossa sincera gratidão por sua contribuição durante este período.</p>
        	            
        	            <h4 style="margin-top: 20px;">Informações Importantes:</h4>
        	            <ul>
        	                <li>Seu acesso à plataforma LUMENS foi desativado</li>
        	                <li>Seus registros permanecerão armazenados conforme exigências legais</li>
        	                <li>Para documentos ou certificados, contate o RH da empresa</li>
        	            </ul>
        	            
        	            <div class="signature">
        	                <p>Atenciosamente,</p>
        	                <p><strong>Equipe %s</strong></p>
        	                <p>Em parceria com LUMENS Plataforma de Gestão</p>
        	            </div>
        	        </div>
        	        
        	        <div class="footer">
        	            <p>Este é um e-mail automático. Não responda diretamente.</p>
        	            <p>© %d LUMENS LTDA. Todos os direitos reservados.</p>
        	        </div>
        	    </body>
        	    </html>
        	""",
        	funcionario.getNomeUser(),
        	empresa.getNomeUser(),
        	LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
        	empresa.getNomeUser(),
        	LocalDate.now().getYear());

        	emailService.enviarEmailHtml(
        	    funcionario.getMail(),
        	    "🔴 Encerramento de Vínculo com " + empresa.getNomeUser(),
        	    mensagemDesligamento
        	);
    }

    @Transactional
    public ResponseEntity<?> alterarDadosUser(AlterRequest alterRequest) {
        try {
            Optional<User> userOpt = userRepository.findByMail(alterRequest.getMail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuário não encontrado com este e-mail");
            }
            User user = userOpt.get();
            String verificationCode = emailVerificationService.generateVerificationCode(alterRequest.getMail());
            String mensagemAlteracaoSenha = String.format("""
            	    <!DOCTYPE html>
            	    <html>
            	    <head>
            	        <style>
            	            body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
            	            .header { background-color: #f8f9fa; padding: 20px; text-align: center; }
            	            .code { font-size: 24px; letter-spacing: 3px; text-align: center; margin: 25px 0; padding: 15px; background-color: #f0f7ff; border-radius: 5px; }
            	            .footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }
            	            .alert { background-color: #fff3cd; padding: 10px; border-radius: 5px; margin: 15px 0; }
            	        </style>
            	    </head>
            	    <body>
            	        <div class="header">
            	            <h3 style="color: #2e59a7;">Segurança da Conta LUMENS</h3>
            	        </div>
            	        
            	        <div class="content">
            	            <p>Olá <strong>%s</strong>,</p>
            	            
            	            <p>Recebemos uma solicitação para alteração da senha da sua conta. Utilize o seguinte código de verificação:</p>
            	            
            	            <div class="code">
            	                🔒 <strong>%s</strong>
            	            </div>
            	            
            	            <p style="text-align: center;">Este código é válido por <strong>5 minutos</strong></p>
            	            
            	            <div class="alert">
            	                <p><strong>Não compartilhe este código</strong> com terceiros, incluindo supostos representantes da LUMENS.</p>
            	            </div>
            	            
            	            <h4>Dicas de Segurança:</h4>
            	            <ul>
            	                <li>Crie uma senha forte com letras, números e caracteres especiais</li>
            	                <li>Não reutilize senhas antigas</li>
            	                <li>Ative a autenticação de dois fatores quando disponível</li>
            	            </ul>
            	            
            	            <p style="margin-top: 20px;"><em>Caso não tenha solicitado esta alteração, recomendamos:</em></p>
            	            <ol>
            	                <li>Alterar sua senha imediatamente</li>
            	                <li>Verificar atividades suspeitas em sua conta</li>
            	            </ol>
            	        </div>
            	        
            	        <div class="footer">
            	            <p>Este e-mail foi enviado automaticamente pela plataforma LUMENS.</p>
            	            <p>© %d LUMENS LTDA. Todos os direitos reservados.</p>
            	        </div>
            	    </body>
            	    </html>
            	""",
            	user.getNomeUser(),
            	verificationCode,
            	LocalDate.now().getYear());

            	emailService.enviarEmailHtml(
            	    alterRequest.getMail(),
            	    "🔑 Código de Verificação para Alteração de Senha",
            	    mensagemAlteracaoSenha
            	);
            return ResponseEntity.ok("Código de verificação enviado para o e-mail");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao processar solicitação: " + e.getMessage());
        }
    }
    
    @Transactional
    public ResponseEntity<?> confirmarAlteracaoSenha(ConfirmacaoSenhaRequest request) {
        try {
            if (!emailVerificationService.verifyCode(request.getMail(), request.getCodigo())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Código inválido, expirado ou já utilizado");
            }
            if (!request.getNovaSenha().equals(request.getConfirmacaoSenha())) {
                return ResponseEntity.badRequest()
                    .body("As senhas não coincidem");
            }
            Optional<User> userOpt = userRepository.findByMail(request.getMail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuário não encontrado");
            }
            User user = userOpt.get();
            user.setSenha(passwordEncoder.encode(request.getNovaSenha()));
            userRepository.save(user);
            emailVerificationService.invalidateCode(request.getMail());
            return ResponseEntity.ok("Senha alterada com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao alterar senha: " + e.getMessage());
        }
    }
}