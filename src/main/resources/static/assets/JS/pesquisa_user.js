// Função para aplicar máscara de CPF
function aplicarMascaraCPF(cpf) {
    cpf = cpf.replace(/\D/g, ''); // Remove tudo que não é dígito
    cpf = cpf.replace(/(\d{3})(\d)/, '$1.$2'); // Coloca um ponto após os 3 primeiros dígitos
    cpf = cpf.replace(/(\d{3})(\d)/, '$1.$2'); // Coloca um ponto após os 6 primeiros dígitos
    cpf = cpf.replace(/(\d{3})(\d{1,2})$/, '$1-$2'); // Coloca um traço antes dos 2 últimos dígitos
    return cpf;
}

// Aplicar máscara ao campo de CPF
document.addEventListener("DOMContentLoaded", function() {
    const cpfInput = document.getElementById("cpfUsuario");
    if (cpfInput) {
        cpfInput.addEventListener("input", function(e) {
            // Salva a posição do cursor
            const cursorPosition = e.target.selectionStart;
            const originalLength = e.target.value.length;
            
            // Aplica a máscara
            e.target.value = aplicarMascaraCPF(e.target.value);
            
            // Restaura a posição do cursor, ajustando para adições/remoções
            const newLength = e.target.value.length;
            const lengthDiff = newLength - originalLength;
            e.target.setSelectionRange(cursorPosition + lengthDiff, cursorPosition + lengthDiff);
        });
    }

    // Configuração do botão de pesquisa (mantendo o código existente)
    document.getElementById("btnPesquisar")?.addEventListener("click", async function(event) {
        event.preventDefault();

        // Captura os valores dos campos de pesquisa de forma segura
        const nomeUsuario = document.getElementById("nomeUsuario")?.value?.trim() || '';
        const emailUsuario = document.getElementById("emailUsuario")?.value?.trim() || '';
        let cpfUsuario = document.getElementById("cpfUsuario")?.value?.trim() || '';
        const funcaoUsuario = document.getElementById("funcaoUsuario")?.value || '';

        // Remove a formatação do CPF antes de enviar
        cpfUsuario = cpfUsuario.replace(/\D/g, '');

        try {
            let url;
            const params = new URLSearchParams();
            
            // Verifica combinações específicas
            if (emailUsuario) {
                // Pesquisa por e-mail
                url = "http://localhost:8080/api/usuario/buscar/email";
                params.append("email", emailUsuario);
            } 
            else if (cpfUsuario) {
                // Pesquisa por CPF (já sem formatação)
                url = "http://localhost:8080/api/usuario/buscar/CPF";
                params.append("CPF", cpfUsuario);
            }
            else if (nomeUsuario) {
                // Pesquisa por nome (com ou sem função)
                url = "http://localhost:8080/api/usuario/buscar/nomeUsuario";
                params.append("nomeUser", nomeUsuario);
            }
            else {
                // Se nenhum campo foi preenchido, busca todos os usuários ativos vinculados ao CNPJ da empresa
                url = "http://localhost:8080/api/usuario/buscar/Ativos";
            }

            // Adiciona a função aos parâmetros se foi especificada
            if (funcaoUsuario && url !== "http://localhost:8080/api/usuario/buscar/Ativos") {
                params.append("function", funcaoUsuario);
            }

            // Faz a requisição ao backend
            const response = await fetch(url + (params.toString() ? `?${params.toString()}` : ''), {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + localStorage.getItem("jwt")
                }
            });

            // Verifica se a resposta foi bem-sucedida
            if (response.ok) {
                const usuarios = await response.json();
                
                // Converte array único para array quando necessário
                let resultadosFinais = Array.isArray(usuarios) ? usuarios : [usuarios];
                
                // Filtra por função se necessário
                if (funcaoUsuario) {
                    resultadosFinais = resultadosFinais.filter(user => user.function === funcaoUsuario);
                }
                
                if (resultadosFinais.length === 0) {
                    showError("Nenhum usuário encontrado com os critérios informados.");
                } else {
                    localStorage.setItem("resultadosPesquisaUsuarios", JSON.stringify(resultadosFinais));
                window.location.href = "/src/main/resources/static/api/usuario/ReturnQueryUser.html";
                }
            } else {
                const errorData = await response.json().catch(() => null);
                const errorMessage = errorData?.error || errorData?.message || "Erro ao realizar a pesquisa.";
                showError(errorMessage);
            }
        } catch (error) {
            console.error("Erro na pesquisa:", error);
            showError("Erro ao conectar com o servidor: " + error.message);
        }
    });
});

// Função para exibir mensagens de erro (mantida igual)
function showError(message) {
    const errorOverlay = document.getElementById("errorOverlay");
    const errorMessage = document.getElementById("errorMessage");
    
    if (errorOverlay && errorMessage) {
        errorMessage.textContent = message;
        errorOverlay.style.display = "flex";

        const closeButton = document.getElementById("closeErrorButton");
        if (closeButton) {
            closeButton.addEventListener("click", function() {
                errorOverlay.style.display = "none";
            });
        }
    } else {
        alert(message); // Fallback caso os elementos de erro não existam
    }
}