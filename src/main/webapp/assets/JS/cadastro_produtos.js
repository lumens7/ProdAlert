document.addEventListener("DOMContentLoaded", function () {
    if (!checkAuth()) {
        return; 
    }
    document.getElementById("quantidadeContada").addEventListener("input", function (e) {
        e.target.value = e.target.value.replace(/[^0-9.]/g, '');
        const parts = e.target.value.split('.');
        if (parts.length > 2) {
            e.target.value = parts[0] + '.' + parts.slice(1).join('');
        }
    });
    document.getElementById("dataValidade").addEventListener("change", function() {
        const dataValidadeInput = this.value;
        if (dataValidadeInput) {
            const [day, month, year] = dataValidadeInput.split('/');
            const dataValidade = new Date(`${year}-${month}-${day}`);
            const hoje = new Date();
            hoje.setHours(0, 0, 0, 0);
            if (dataValidade < hoje) {
                showError("A data de validade não pode ser anterior à data atual.", null, 0, false);
                this.value = ''; 
            }
        }
    });

    document.getElementById("produtoForm").addEventListener("submit", async function(event) {
        event.preventDefault();
        const quantidadeContada = document.getElementById("quantidadeContada").value;
        if (!quantidadeContada || isNaN(quantidadeContada)) {
            showError("A quantidade deve ser um número válido.", null, 0, false);
            return;
        }
        const codBarras = document.getElementById("codBarras").value;
        if (!codBarras || codBarras.length > 20) {
            showError("O código de barras deve ter no máximo 20 caracteres.", null, 0, false);
            return;
        }
        const dataValidadeInput = document.getElementById("dataValidade").value;
        if (!dataValidadeInput) {
            showError("A data de validade é obrigatória.", null, 0, false);
            return;
        }
        const [day, month, year] = dataValidadeInput.split('/');
        const dataValidade = new Date(`${year}-${month}-${day}`);
        const hoje = new Date();
        hoje.setHours(0, 0, 0, 0);
        if (dataValidade < hoje) {
            showError("A data de validade não pode ser anterior à data atual.", null, 0, false);
            return;
        }
        const produto = {
            nomeProduto: document.getElementById("nomeProduto").value,
            descricaoProduto: document.getElementById("descricaoProduto").value, 
            codBarras: codBarras,
            dataValidade: dataValidadeInput,
            quantidadeContada: parseFloat(quantidadeContada) 
        };
        try {
            const response = await fetch("http://localhost:8080/api/produto/cadastro", {
                method: "POST",
                headers: { 
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + localStorage.getItem("jwt")
                },
                body: JSON.stringify(produto)
            });

            if (response.ok) {
                const nomeProduto = document.getElementById("nomeProduto").value;
                showSuccess(`Produto ${nomeProduto} cadastrado com sucesso!`, "/api/produto/cadastro.html", 5000);
                document.getElementById("produtoForm").reset();
            } else {
                const errorData = await response.json();
                const errorMessage = errorData.message || "Erro inesperado ao cadastrar produto.";
                showError(errorMessage, null, 0, false);
            }
        } catch (error) {
            showError("Erro ao conectar com o servidor: " + error.message, null, 0, false);
        }
    });
});