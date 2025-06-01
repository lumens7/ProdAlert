package br.com.lumens.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.lumens.DOMAIN.Produto;
import br.com.lumens.DTO.ProdutoRequest;
import br.com.lumens.Repository.ProdutoRepository;
import br.com.lumens.Repository.UserRepository;
import br.com.lumens.Service.ProdutoService;

/*
Criado por Luís
*/

@RestController
@RequestMapping("/api/produto")
public class ProdutoController {
	
	@Autowired
    private ProdutoService prodService;
    
    private UserRepository userRepository;
    
    private ProdutoRepository prodRepository;

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrarProduto(@RequestBody ProdutoRequest produtoRequest) {
        try {
        	if (produtoRequest.getDataValidadeAsLocalDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Data de validade não pode ser anterior à data atual!");
            }
            Produto produto = new Produto();
            produto.setNomeProduto(produtoRequest.getNomeProduto());
            produto.setDescricaoProduto(produtoRequest.getDescricaoProduto());
            produto.setCodBarras(produtoRequest.getCodBarras());
            produto.setDataValidade(produtoRequest.getDataValidadeAsLocalDate());
            produto.setQuantidadeContada(produtoRequest.getQuantidadeContada());

            prodService.cadastrarProduto(produto);
            return ResponseEntity.ok("Produto cadastrado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro inesperado ao cadastrar produto: " + e.getMessage()));
        }
    }
    
    @GetMapping("/buscar/nome")
    public ResponseEntity<?> buscarProdutosPorNome(@RequestParam("nome") String nome) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Produto> produtos = prodService.buscarProdutosPorNome(nome, email);
            
            if (produtos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum produto encontrado");
            }
            return ResponseEntity.ok(produtos);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao buscar produtos: " + e.getMessage());
        }
    }

    
    @GetMapping("/buscar/codigo")
    public ResponseEntity<?> buscarPorCodigoBarras(@RequestParam("codBarras") String codigo) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Produto> produtos = prodService.buscarPorCodigoBarras(codigo, email);
            
            if (produtos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum produto encontrado com o Código de Barras Informado.");
            }
            
            return ResponseEntity.ok(produtos);
        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar produtos: " + e.getMessage());
        }
    }
    
    @GetMapping("/buscar/intervaloDataContagem")
    public ResponseEntity<?> buscarPorIntervaloDataContagem(
            @RequestParam("dataContagemInicial") String dataContagemInicial,
            @RequestParam("dataContagemFinal") String dataContagemFinal) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Produto> produtos = prodService.buscarPorIntervaloDataContagem(dataContagemInicial, dataContagemFinal, email);
            
            if (produtos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum produto encontrado no intervalo de data de contagem informado.");
            }
            
            return ResponseEntity.ok(produtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar produtos: " + e.getMessage());
        }
    }

    @GetMapping("/buscar/intervaloDataValidade")
    public ResponseEntity<?> buscarPorIntervaloDataValidade(
            @RequestParam("dataValidadeInicial") String dataValidadeInicial,
            @RequestParam("dataValidadeFinal") String dataValidadeFinal) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Produto> produtos = prodService.buscarPorIntervaloDataValidade(dataValidadeInicial, dataValidadeFinal, email);
            
            if (produtos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum produto encontrado no intervalo de data de validade informado.");
            }
            
            return ResponseEntity.ok(produtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar produtos: " + e.getMessage());
        }
    }
    
    @GetMapping("/buscar/NomeProdutoeDataContagem")
    public ResponseEntity<?> buscarPorDataContagemENomeProduto(
            @RequestParam("nomeProduto") String nomeProduto, 
            @RequestParam("dataContagemInicial") String dataContagemInicial,
            @RequestParam("dataContagemFinal") String dataContagemFinal) {
        try {
            
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Produto> produtos = prodService.buscarPorDataContagemENomeProduto(
                nomeProduto, dataContagemInicial, dataContagemFinal, email);
            
            if (produtos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Nenhum produto encontrado com os critérios informados.");
            }
            
            return ResponseEntity.ok(produtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao buscar produtos: " + e.getMessage());
        }
    }
    
    @GetMapping("/buscar/NomeProdutoeDataValidade")
    public ResponseEntity<?> buscarPorDataValidadeENomeProduto(
            @RequestParam("nomeProduto") String nomeProduto, 
            @RequestParam("dataValidadeInicial") String dataValidadeInicial,
            @RequestParam("dataValidadeFinal") String dataValidadeFinal) {
        try {
            
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Produto> produtos = prodService.buscarPorDataValidadeENomeProduto(
                nomeProduto, dataValidadeInicial, dataValidadeFinal, email);
            
            if (produtos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Nenhum produto encontrado com os critérios informados.");
            }
            
            return ResponseEntity.ok(produtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao buscar produtos: " + e.getMessage());
        }
    }
    
    @GetMapping("/buscar/Ativos")
    public ResponseEntity<?> buscarTodos() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Produto> produtosAtivos = prodService.buscarTodos(email);
            
            if (produtosAtivos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum produto ativo encontrado.");
            }
            
            return ResponseEntity.ok(produtosAtivos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar produtos ativos: " + e.getMessage());
        }
    }
    
    @GetMapping("/buscar/ultimos6")
    public ResponseEntity<?> buscarUltimosProdutosDaEmpresa() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Produto> produtos = prodService.buscarUltimosProdutosDaEmpresa(email);
            
            if (produtos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum produto encontrado.");
            }
            return ResponseEntity.ok(produtos);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao buscar produtos: " + e.getMessage());
        }
    }
    
}
