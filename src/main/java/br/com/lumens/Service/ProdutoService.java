package br.com.lumens.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.lumens.DOMAIN.Produto;
import br.com.lumens.DOMAIN.User;
import br.com.lumens.DOMAIN.Vincular;
import br.com.lumens.Repository.ProdutoRepository;
import br.com.lumens.Repository.UserRepository;
import br.com.lumens.Repository.VincularRepository;

/*
Criado por Luís
*/

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VincularRepository vincularRepository;
    
    @Autowired
    private UserService userService;
    private static final List<Integer> DIAS_ALERTA = Arrays.asList(30, 15, 7, 3, 0); 
    public void cadastrarProduto(Produto produto) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String mail;

            if (principal instanceof UserDetails) {
                mail = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                mail = (String) principal;
            } else {
                throw new IllegalStateException("Tipo inesperado de principal: " + principal.getClass().getName());
            }
            User funcionario = userRepository.findByMail(mail)
                .orElseThrow(() -> {
                    return new UsernameNotFoundException("Usuário não encontrado!");
                });
            if (funcionario.getFunction() == User.functionRole.FUNCIONARIO) {
                List<Vincular> vinculos = vincularRepository.findByCPF(funcionario.getCPF());
                if (vinculos == null || vinculos.isEmpty()) {
                    throw new IllegalStateException("Acesso inválido, você não tem nenhum vínculo com alguma empresa.");
                }
            }
            if (produto.getDataValidade() == null) {
                throw new IllegalArgumentException("Data de validade é obrigatória!");
            }
            if (produto.getDataValidade().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Data de validade não pode ser anterior à data atual!");
            }
            produto.setDataContagem(LocalDate.now());
            if (produto.getCodBarras() == null || produto.getCodBarras().isEmpty()) {
                throw new IllegalArgumentException("Código de barras é obrigatório!");
            } else if (produto.getCodBarras().length() > 20) {
                throw new IllegalArgumentException("Código de barras deve ter no máximo 20 caracteres!");
            }
            produto.setStatus(Produto.Status.ATIVO);
            produto.setFuncionario(funcionario);
            produtoRepository.save(produto);
        } catch (Exception e) {
            throw new RuntimeException("Erro inesperado ao cadastrar produto.", e);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") 
    public void inativarProdutosVencidos() {
        try {
            produtoRepository.inativarProdutosVencidos();
        } catch (Exception e) {
        	throw e;
        }
    }
    
    public List<Produto> buscarProdutosPorNome(String nome, String emailUsuario) {
        try {
            Optional<User> usuarioOpt = userService.buscarPorMail(emailUsuario);
            if (usuarioOpt.isEmpty()) {
                throw new IllegalArgumentException("Usuário não encontrado");
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
                    throw new IllegalArgumentException("Funcionário não vinculado a uma empresa");
                }
            } else if (usuario.getFunction() == User.functionRole.EMPRESA) {
                cnpjEmpresa = usuario.getCNPJ();
            }
            if (cnpjEmpresa == null) {
                throw new IllegalArgumentException("Não foi possível identificar a empresa");
            }
            return produtoRepository.findByNomeAndEmpresa(nome, cnpjEmpresa);
        } catch (Exception e) {
            throw e;
        }
    }
    private String getCnpjEmpresaUsuario(String emailUsuario) {
        Optional<User> usuarioOpt = userService.buscarPorMail(emailUsuario);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        User usuario = usuarioOpt.get();
        String cnpjEmpresa = null;
        if (usuario.getFunction() == User.functionRole.FUNCIONARIO) {
            Optional<Vincular> vinculo = vincularRepository.findByCPF(usuario.getCPF()).stream().findFirst();
            if (vinculo.isPresent()) {
                cnpjEmpresa = vinculo.get().getCNPJ();
            } else {
                throw new IllegalArgumentException("Funcionário não vinculado a uma empresa");
            }
        } else if (usuario.getFunction() == User.functionRole.EMPRESA) {
            cnpjEmpresa = usuario.getCNPJ();
        }
        if (cnpjEmpresa == null) {
            throw new IllegalArgumentException("Não foi possível identificar a empresa");
        }
        return cnpjEmpresa;
    }

    
    public List<Produto> buscarPorCodigoBarras(String codigo, String emailUsuario) {
        try {
            String cnpjEmpresa = getCnpjEmpresaUsuario(emailUsuario);
            if (codigo == null || codigo.trim().isEmpty()) {
                throw new IllegalArgumentException("O código de barras não pode ser nulo ou vazio.");
            }
            if (codigo.length() < 13) {
                codigo = String.format("%013d", Long.parseLong(codigo));
            }
            List<Produto> produtos = produtoRepository.findByCodBarrasAndEmpresa(codigo, cnpjEmpresa);
            return produtos;
        } catch (Exception e) {
            throw e;
        }
    }
    public List<Produto> buscarPorIntervaloDataContagem(String dataContagemInicial, String dataContagemFinal, String emailUsuario) {
        try {
            String cnpjEmpresa = getCnpjEmpresaUsuario(emailUsuario);
            LocalDate dataInicial = parseDate(dataContagemInicial);
            LocalDate dataFinal = parseDate(dataContagemFinal);
            List<Produto> produtos = produtoRepository.findByDataContagemBetweenAndEmpresa(dataInicial, dataFinal, cnpjEmpresa);
            return produtos;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data inválido. Use o padrão ddMMyyyy (ex: 31012025).");
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Produto> buscarPorIntervaloDataValidade(String dataValidadeInicial, String dataValidadeFinal, String emailUsuario) {
        try {
            String cnpjEmpresa = getCnpjEmpresaUsuario(emailUsuario);
            LocalDate dataInicial = parseDate(dataValidadeInicial);
            LocalDate dataFinal = parseDate(dataValidadeFinal);
            List<Produto> produtos = produtoRepository.findByDataValidadeBetweenAndEmpresa(dataInicial, dataFinal, cnpjEmpresa);
            return produtos;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data inválido. Use o padrão ddMMyyyy (ex: 31012025).");
        } catch (Exception e) {
            throw e;
        }
    }

    private LocalDate parseDate(String date) {
        String dataFormatada = date.substring(0, 2) + "/" + date.substring(2, 4) + "/" + date.substring(4);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.parse(dataFormatada, formatter);
    }
   
    public List<Produto> buscarPorDataContagemENomeProduto(String nomeProduto, 
            String dataContagemInicial, String dataContagemFinal, String emailUsuario) {
        try {
            String cnpjEmpresa = getCnpjEmpresaUsuario(emailUsuario);
            if (nomeProduto == null || nomeProduto.trim().isEmpty()) {
                throw new IllegalArgumentException("O nome do produto não pode ser nulo ou vazio.");
            }
            if (dataContagemInicial == null || dataContagemInicial.trim().isEmpty() || 
                dataContagemFinal == null || dataContagemFinal.trim().isEmpty()) {
                throw new IllegalArgumentException("Ambas as datas (inicial e final) devem ser informadas.");
            }
            LocalDate dataInicial = parseDate(dataContagemInicial);
            LocalDate dataFinal = parseDate(dataContagemFinal);
            
            if (dataInicial.isAfter(dataFinal)) {
                throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
            }
            List<Produto> produtos = produtoRepository.findByNomeProdutoAndDataContagemBetweenAndEmpresa(
                nomeProduto, dataInicial, dataFinal, cnpjEmpresa);
            return produtos;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data inválido. Use o padrão ddMMyyyy (ex: 31012025).");
        } catch (Exception e) {
            throw e;
        }
    }
    
    public List<Produto> buscarPorDataValidadeENomeProduto(String nomeProduto, 
            String dataValidadeInicial, String dataValidadeFinal, String emailUsuario) {
        try {
            String cnpjEmpresa = getCnpjEmpresaUsuario(emailUsuario);
            if (nomeProduto == null || nomeProduto.trim().isEmpty()) {
                throw new IllegalArgumentException("O nome do produto não pode ser nulo ou vazio.");
            }
            if (dataValidadeInicial == null || dataValidadeInicial.trim().isEmpty() || 
                dataValidadeFinal == null || dataValidadeFinal.trim().isEmpty()) {
                throw new IllegalArgumentException("Ambas as datas (inicial e final) devem ser informadas.");
            }
            LocalDate dataInicial = parseDate(dataValidadeInicial);
            LocalDate dataFinal = parseDate(dataValidadeFinal);
            if (dataInicial.isAfter(dataFinal)) {
                throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
            }
            List<Produto> produtos = produtoRepository.findByNomeProdutoAndDataValidadeBetweenAndEmpresa(
                nomeProduto, dataInicial, dataFinal, cnpjEmpresa);
            return produtos;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data inválido. Use o padrão ddMMyyyy (ex: 31012025).");
        } catch (Exception e) {
            throw e;
        }
    }
    
    public List<Produto> buscarTodos(String emailUsuario) {
        try {
            String cnpjEmpresa = getCnpjEmpresaUsuario(emailUsuario);
            List<Produto> produtos = produtoRepository.findAllByEmpresa(cnpjEmpresa);
            return produtos;
        } catch (Exception e) {
            throw e;
        }
    }
    
    public List<Produto> buscarUltimosProdutosDaEmpresa(String emailUsuarioLogado) {
        try {
            Optional<User> usuarioOpt = userService.buscarPorMail(emailUsuarioLogado);
            if (usuarioOpt.isEmpty()) {
                throw new IllegalArgumentException("Usuário não encontrado.");
            }
            User usuario = usuarioOpt.get();
            String cnpjEmpresa = null;
            if (usuario.getFunction() == User.functionRole.FUNCIONARIO) {
                Optional<Vincular> vinculo = vincularRepository.findByCPF(usuario.getCPF())
                    .stream()
                    .findFirst();
                if (vinculo.isPresent()) {
                    cnpjEmpresa = vinculo.get().getCNPJ();
                }
            } else if (usuario.getFunction() == User.functionRole.EMPRESA) {
                cnpjEmpresa = usuario.getCNPJ();
            }
            if (cnpjEmpresa == null) {
                throw new IllegalArgumentException("Não foi possível identificar a empresa.");
            }
            Pageable pageable = PageRequest.of(0, 6);
            return produtoRepository.findTop6ByEmpresaOrderByDataContagemDesc(cnpjEmpresa, pageable);
        } catch (Exception e) {
            throw e;
        }
    }
    
    @Scheduled(cron = "0 0 1 * * *")
    public void verificarVencimentosEEnviarEmails() {
        try {
            LocalDate hoje = LocalDate.now();
            List<LocalDate> datasAlerta = DIAS_ALERTA.stream().map(hoje::plusDays).toList();
            List<Produto> produtos = produtoRepository.findByDataValidadeIn(datasAlerta);
            for (Produto produto : produtos) {
                User funcionario = produto.getFuncionario();
                if (funcionario != null && funcionario.getCPF() != null) {
                    Optional<Vincular> vinculo = vincularRepository.findByCPF(funcionario.getCPF())
                        .stream()
                        .findFirst();
                    if (vinculo.isPresent()) {
                        Optional<User> empresaOpt = userRepository.findByCNPJ(vinculo.get().getCNPJ());
                        if (empresaOpt.isPresent() && empresaOpt.get().getFunction() == User.functionRole.EMPRESA) {
                            List<Vincular> vinculosFuncionarios = vincularRepository.findByCNPJ(vinculo.get().getCNPJ());
                            Set<String> emailsFuncionarios = vinculosFuncionarios.stream()
                                .map(v -> userRepository.findByCPF(v.getCPF()))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(User::getMail)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());
                            for (String email : emailsFuncionarios) {
                                enviarAlertaVencimento(email, produto);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        	throw e;
        }
    }

    private void enviarAlertaVencimento(String emailFuncionario, Produto produto) {
        try {
            String assunto = "🟠 Alerta: " + produto.getNomeProduto() + " próximo do vencimento";
            String mensagem = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #f8f9fa; padding: 15px; text-align: center; }
                        .content { padding: 20px; }
                        .alert-box { 
                            border-left: 4px solid #ffc107; 
                            background-color: #fff8e1; 
                            padding: 15px; 
                            margin: 15px 0;
                        }
                        .product-info { margin: 15px 0; }
                        .footer { margin-top: 20px; font-size: 12px; color: #666; }
                        .button {
                            background-color: #28a745;
                            color: white;
                            padding: 10px 15px;
                            text-decoration: none;
                            border-radius: 4px;
                            display: inline-block;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>LUMENS - Gestão de Produtos</h2>
                        </div>
                        
                        <div class="content">
                            <h3>Alerta de Vencimento</h3>
                            
                            <div class="alert-box">
                                <p>O produto abaixo está próximo da data de vencimento:</p>
                            </div>
                            
                            <div class="product-info">
                                <p><strong>Produto:</strong> %s</p>
                                <p><strong>Código:</strong> %s</p>
                                <p><strong>Data de Vencimento:</strong> <span style="color: #dc3545;">%s</span></p>
                                <p><strong>Dias restantes:</strong> %d dia(s)</p>
                            </div>
                            
                            <h4>Ações Recomendadas:</h4>
                            <ul>
                                <li>Verifique o estoque físico</li>
                                <li>Considere aplicar promoção para escoamento</li>
                                <li>Analise possibilidade de devolução ao fornecedor</li>
                                <li>Registre qualquer ação tomada no sistema</li>
                            </ul>
                        </div>
                        
                        <div class="footer">
                            <p>Este é um e-mail automático. Por favor não responda diretamente.</p>
                            <p>© %d LUMENS LTDA. Todos os direitos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
            """, 
            produto.getNomeProduto(),
            produto.getCodBarras() != null ? produto.getCodBarras() : "N/A",
            produto.getDataValidade().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            ChronoUnit.DAYS.between(LocalDate.now(), produto.getDataValidade()),
            produto.getIdProduto(),
            LocalDate.now().getYear()
            );
            emailService.enviarEmailHtml(emailFuncionario, assunto, mensagem);
        } catch (Exception e) {
        	throw e;
        }
    }

}