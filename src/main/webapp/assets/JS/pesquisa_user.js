function aplicarMascaraCPF(cpf) {
    cpf = cpf.replace(/\D/g, ''); 
    cpf = cpf.replace(/(\d{3})(\d)/, '$1.$2');
    cpf = cpf.replace(/(\d{3})(\d)/, '$1.$2'); 
    cpf = cpf.replace(/(\d{3})(\d{1,2})$/, '$1-$2'); 
    return cpf;
}

document.addEventListener("DOMContentLoaded", function() {
    const cpfInput = document.getElementById("cpfUsuario");
    if (cpfInput) {
        cpfInput.addEventListener("input", function(e) {
            const cursorPosition = e.target.selectionStart;
            const originalLength = e.target.value.length;
            e.target.value = aplicarMascaraCPF(e.target.value);
            const newLength = e.target.value.length;
            const lengthDiff = newLength - originalLength;
            e.target.setSelectionRange(cursorPosition + lengthDiff, cursorPosition + lengthDiff);
        });
    }
    document.getElementById("btnPesquisar")?.addEventListener("click", async function(event) {
        event.preventDefault();
        const nomeUsuario = document.getElementById("nomeUsuario")?.value?.trim() || '';
        const emailUsuario = document.getElementById("emailUsuario")?.value?.trim() || '';
        let cpfUsuario = document.getElementById("cpfUsuario")?.value?.trim() || '';
        const funcaoUsuario = document.getElementById("funcaoUsuario")?.value || '';
        cpfUsuario = cpfUsuario.replace(/\D/g, '');
        try {
            let url;
            const params = new URLSearchParams();
            if (emailUsuario) {
                url = "http://localhost:8080/api/usuario/buscar/email";
                params.append("email", emailUsuario);
            } 
            else if (cpfUsuario) {
                url = "http://localhost:8080/api/usuario/buscar/CPF";
                params.append("CPF", cpfUsuario);
            }
            else if (nomeUsuario) {
                url = "http://localhost:8080/api/usuario/buscar/nomeUsuario";
                params.append("nomeUser", nomeUsuario);
            }
            else {
                url = "http://localhost:8080/api/usuario/buscar/Ativos";
            }
            if (funcaoUsuario && url !== "http://localhost:8080/api/usuario/buscar/Ativos") {
                params.append("function", funcaoUsuario);
            }
            const response = await fetch(url + (params.toString() ? `?${params.toString()}` : ''), {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + localStorage.getItem("jwt")
                }
            });
            if (response.ok) {
                const usuarios = await response.json();
                let resultadosFinais = Array.isArray(usuarios) ? usuarios : [usuarios];
                if (funcaoUsuario) {
                    resultadosFinais = resultadosFinais.filter(user => user.function === funcaoUsuario);
                }
                if (resultadosFinais.length === 0) {
                    showError("Nenhum usuário encontrado com os critérios informados.");
                } else {
                    localStorage.setItem("resultadosPesquisaUsuarios", JSON.stringify(resultadosFinais));
                window.location.href = "/api/usuario/ReturnQueryUser.html";
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
        alert(message); 
    }
}