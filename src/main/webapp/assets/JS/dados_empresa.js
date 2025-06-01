document.addEventListener("DOMContentLoaded", function () {
    if (!checkAuth()) {
        return; 
    }
    fetch("http://localhost:8080/api/usuario/dados_usuario", {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("jwt")
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Erro ao buscar dados da empresa.");
        }
        return response.json();
    })
    .then(data => {
        console.log("Dados do usuário:", data);
        const dadosEmpresa = document.getElementById("dados-empresa");
        if (!dadosEmpresa) {
            throw new Error("Elemento 'dados-empresa' não encontrado.");
        }
        const formatCNPJ = (cnpj) => {
            return cnpj.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
        };

        dadosEmpresa.innerHTML = `
            <p><strong>Nome:</strong><p> ${data.nomeUser}</p>
            <p><strong>CNPJ:</strong><p> ${formatCNPJ(data.CNPJ)}</p>
            <p><strong>E-mail:</strong><p> ${data.mail}</p>
        `;
    })
    .catch(error => {
        console.error("Erro ao buscar dados da empresa:", error);
        showError("Erro ao buscar dados da empresa: " + error.message, null, 0, false);
    });
});