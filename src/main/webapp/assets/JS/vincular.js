document.addEventListener("DOMContentLoaded", function () {
    if (!localStorage.getItem("jwt")) {
        window.location.href = "/login.html";
        return;
    }
    const form = document.getElementById("vincularForm");
    const cpfInput = document.getElementById("CPF");
    form.addEventListener("submit", async function (event) {
        event.preventDefault();
        const cpf = cpfInput.value.replace(/\D/g, '');
        if (cpf.length !== 11) {
            showError("CPF inválido. Deve conter 11 dígitos.");
            return;
        }
        try {
            const response = await fetch(`http://localhost:8080/api/usuario/buscarPorCPF?CPF=${cpf}`, {
                headers: { 
                    "Authorization": "Bearer " + localStorage.getItem("jwt") 
                }
            });

            if (!response.ok) {
                throw new Error(await response.text());
            }

            const funcionarioData = await response.json();
            showFuncionarioData(funcionarioData);
            
        } catch (error) {
            showError(error.message);
        }
    });
    function showFuncionarioData(data) {
        const overlay = document.getElementById("confirmOverlay");
        const dadosContainer = document.getElementById("dadosFuncionario");
        const confirmMessage = document.getElementById("confirmMessage");
        dadosContainer.innerHTML = `
            <p><strong>Nome:</strong> ${data.nomeUser}</p>
            <p><strong>CPF:</strong> ${formatCPF(data.CPF)}</p>
            <p><strong>E-mail:</strong> ${data.mail}</p>
        `;
        confirmMessage.textContent = data.status === "INATIVO" 
            ? "Funcionário está INATIVO. Deseja ativar e vincular?" 
            : "Deseja vincular este funcionário?";
        overlay.style.display = "flex";
        document.getElementById("confirmAction").onclick = async () => {
            try {
                if (data.status === "INATIVO") {
                    await ativarFuncionario(data.CPF);
                }
                await vincularFuncionario(data.CPF);
                overlay.style.display = "none";
                alert("Funcionário vinculado com sucesso!");
                window.location.href = "/api/usuario/vincular.html";
            } catch (error) {
                showError(error.message);
            }
        };
        
        document.getElementById("cancelAction").onclick = 
        document.getElementById("closeConfirmButton").onclick = () => {
            overlay.style.display = "none";
        };
    }
    function formatCPF(cpf) {
        return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    }
    async function ativarFuncionario(cpf) {
        const response = await fetch(`http://localhost:8080/api/usuario/ativarFuncionario?CPF=${cpf}`, {
            method: "POST",
            headers: { 
                "Authorization": "Bearer " + localStorage.getItem("jwt") 
            }
        });
        if (!response.ok) throw new Error(await response.text());
    }
    async function vincularFuncionario(cpf) {
        const response = await fetch(`http://localhost:8080/api/usuario/vincular?cpf=${cpf}`, {
            method: "POST",
            headers: { 
                "Authorization": "Bearer " + localStorage.getItem("jwt") 
            }
        });
        if (!response.ok) throw new Error(await response.text());
    }
    function showError(message) {
        const errorOverlay = document.getElementById("errorOverlay");
        const errorMessage = document.getElementById("errorMessage");
        
        if (errorOverlay && errorMessage) {
            errorMessage.textContent = message;
            errorOverlay.style.display = "flex";
            
            document.getElementById("closeErrorButton").onclick = () => {
                errorOverlay.style.display = "none";
            };
        } else {
            alert(message); 
        }
    }
});