document.addEventListener("DOMContentLoaded", function () {
    // Verifica autenticação e permissões ao carregar a página
    if (!checkAuth()) {
        return; // Interrompe a execução se o usuário não estiver autenticado
    }

    // Busca os dados do usuário logado
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
        console.log("Dados do usuário:", data); // Log para depuração

        const dadosUsuario = document.getElementById("dados-usuario");
        if (!dadosUsuario) {
            throw new Error("Elemento 'dados-usuario' não encontrado.");
        }

        // Acessa os dados do usuário
        const nomeUser = data.nomeUser || "N/A";
        const mail = data.mail || "N/A";
        const CPF = formatarCPF(data.CPF) || "N/A";
        const empresaNome = data.empresa ? data.empresa.nomeUser : "N/A";
        const empresaCNPJ = data.empresa ? formatarCNPJ(data.empresa.CNPJ) : "N/A";

        // Exibe os dados na página
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

    // Função para formatar CPF (000.000.000-00)
    function formatarCPF(cpf) {
        if (!cpf) return "N/A";
        // Remove todos os caracteres não numéricos
        cpf = cpf.replace(/\D/g, '');
        
        // Verifica se tem 11 dígitos
        if (cpf.length !== 11) return cpf; // Retorna sem formatação se não for válido
        
        // Aplica a formatação
        return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    }

    // Função para formatar CNPJ (00.000.000/0000-00)
    function formatarCNPJ(cnpj) {
        if (!cnpj) return "N/A";
        // Remove todos os caracteres não numéricos
        cnpj = cnpj.replace(/\D/g, '');
        
        // Verifica se tem 14 dígitos
        if (cnpj.length !== 14) return cnpj; // Retorna sem formatação se não for válido
        
        // Aplica a formatação
        return cnpj.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
    }
});