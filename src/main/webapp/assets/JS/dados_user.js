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
            throw new Error("Erro ao buscar dados do usuário.");
        }
        return response.json();
    })
    .then(data => {
        console.log("Dados do usuário:", data);
        const dadosUsuario = document.getElementById("dados-usuario");
        if (!dadosUsuario) {
            throw new Error("Elemento 'dados-usuario' não encontrado.");
        }
        const nomeUser = data.nomeUser || "N/A";
        const mail = data.mail || "N/A";
        const CPF = formatarCPF(data.CPF) || "N/A";
        const empresaNome = data.empresa ? data.empresa.nomeUser : "N/A";
        const empresaCNPJ = data.empresa ? formatarCNPJ(data.empresa.CNPJ) : "N/A";
        dadosUsuario.innerHTML = `
            <p><strong>Nome:</strong><p> ${nomeUser}</p>
            <p><strong>E-mail:</strong><p> ${mail}</p>
            <p><strong>CPF:</strong><p> ${CPF}</p>
            <p><strong>Empresa:</strong><p> ${empresaNome}</p>
            <p><strong>CNPJ:</strong><p> ${empresaCNPJ}</p>
        `;
    })
    .catch(error => {
        console.error("Erro ao buscar dados do usuário:", error);
        showError("Erro ao buscar dados do usuário: " + error.message, null, 0, false);
    });
    function formatarCPF(cpf) {
        if (!cpf) return "N/A";
        cpf = cpf.replace(/\D/g, '');
        if (cpf.length !== 11) return cpf; 
        return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    }
    function formatarCNPJ(cnpj) {
        if (!cnpj) return "N/A";
        cnpj = cnpj.replace(/\D/g, '');
        if (cnpj.length !== 14) return cnpj; 
        return cnpj.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
    }
});