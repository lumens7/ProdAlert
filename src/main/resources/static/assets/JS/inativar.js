document.addEventListener("DOMContentLoaded", function () {
    // Verificação básica de autenticação
    if (!localStorage.getItem("jwt")) {
        window.location.href = "/login.html";
        return;
    }

    const form = document.getElementById("inativarForm");
    const cpfInput = document.getElementById("CPF");

    form.addEventListener("submit", async function (event) {
        event.preventDefault();
        
        const cpf = cpfInput.value.replace(/\D/g, '');
        if (cpf.length !== 11) {
            showError("CPF inválido. Deve conter 11 dígitos.");
            return;
        }

        try {
            console.log("Buscando funcionário com CPF:", cpf);
            
            const response = await fetch(`http://localhost:8080/api/usuario/buscar/CPF?CPF=${cpf}`, {
                headers: { 
                    "Authorization": "Bearer " + localStorage.getItem("jwt"),
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || "Erro ao buscar funcionário");
            }

            const funcionarioData = await response.json();
            //console.log("Dados recebidos do backend:", JSON.stringify(funcionarioData)); // Log completo
            
            // Verificação robusta dos dados recebidos
            if (!funcionarioData || !funcionarioData.cpf) {
                throw new Error("Dados do funcionário incompletos (CPF faltando)");
            }

            // Normaliza os nomes dos campos para garantir compatibilidade
            const funcionario = {
                nome: funcionarioData.nomeUser || funcionarioData.nome || "Nome não disponível",
                cpf: funcionarioData.cpf || funcionarioData.CPF || "",
                email: funcionarioData.mail || funcionarioData.email || "Email não disponível",
            };

            // Verifica se o funcionário já está inativo
            if (funcionario.status === "INATIVO") {
                showError("Este funcionário já está inativo.");
                return;
            }

            showFuncionarioData(funcionario);
            
        } catch (error) {
            console.error("Erro durante o processo:", error);
            showError(error.message);
        }
    });

    function showFuncionarioData(funcionario) {
        const overlay = document.getElementById("confirmOverlay");
        const dadosContainer = document.getElementById("dadosFuncionario");
        const confirmMessage = document.getElementById("confirmMessage");
        
        try {
            // Preenche os dados do funcionário com os campos normalizados
            dadosContainer.innerHTML = `
                <p><strong>Nome:</strong> ${funcionario.nome}</p>
                <p><strong>CPF:</strong> ${formatCPF(funcionario.cpf)}</p>
                <p><strong>E-mail:</strong> ${funcionario.email}</p>
            `;
            
            // Configura a mensagem de confirmação
            confirmMessage.textContent = "Deseja realmente inativar este funcionário?";
            
            // Mostra o overlay
            overlay.style.display = "flex";
            
            // Configura os botões
            document.getElementById("confirmAction").onclick = async () => {
                try {
                    console.log("Iniciando inativação para CPF:", funcionario.cpf);
                    const response = await inativarFuncionario(funcionario.cpf);
                    
                    if (response.ok) {
                        overlay.style.display = "none";
                        showSuccess(
                            `Funcionário ${funcionario.nome} inativado com sucesso!`,
                            "/src/main/resources/static/api/usuario/inativar.html",
                            5000
                        );
                    } else {
                        const errorText = await response.text();
                        throw new Error(errorText || "Erro ao inativar funcionário");
                    }
                } catch (error) {
                    console.error("Erro durante inativação:", error);
                    showError(error.message);
                }
            };
            
            document.getElementById("cancelAction").onclick = 
            document.getElementById("closeConfirmButton").onclick = () => {
                overlay.style.display = "none";
            };
            
        } catch (error) {
            console.error("Erro ao exibir dados do funcionário:", error);
            showError(error.message);
        }
    }

    function formatCPF(cpf) {
        if (!cpf) return "Não informado";
        // Remove caracteres não numéricos e formata
        const cleaned = cpf.toString().replace(/\D/g, '');
        return cleaned.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    }

    async function inativarFuncionario(cpf) {
        try {
            const response = await fetch(`http://localhost:8080/api/usuario/inativar?CPF=${cpf}`, {
                method: "POST",
                headers: { 
                    "Authorization": "Bearer " + localStorage.getItem("jwt"),
                    "Content-Type": "application/json"
                }
            });
    
            // Primeiro verifique o status da resposta
            if (!response.ok) {
                // Crie uma cópia do response para ler o texto sem consumir o stream
                const responseClone = response.clone();
                let errorMessage = "Erro ao inativar funcionário";
                
                try {
                    // Tente ler como JSON primeiro
                    const errorData = await responseClone.json();
                    errorMessage = errorData.message || errorData.error || errorMessage;
                } catch (e) {
                    // Se falhar, leia como texto simples
                    const errorText = await response.text();
                    errorMessage = errorText || errorMessage;
                }
                
                throw new Error(errorMessage);
            }
            
            // Se chegou aqui, a requisição foi bem-sucedida
            return response;
        } catch (error) {
            console.error("Erro na requisição de inativação:", error);
            throw new Error(error.message || "Falha na comunicação com o servidor");
        }
    }
});