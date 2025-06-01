document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem("jwt");
    if (!token) {
        showError("Você precisa estar logado para acessar esta página.", "/api/usuario/login.html", 5000, true);
        return;
    }
    const payload = JSON.parse(atob(token.split('.')[1])); 
    const expirationDate = new Date(payload.exp * 1000);
    if (expirationDate < new Date()) {
        localStorage.removeItem("jwt");
        showError("Sua sessão expirou. Faça login novamente.", "/api/usuario/login.html", 5000, true);
        return;
    }
    const emailUsuario = payload.sub; 
    if (!emailUsuario) {
        showError("Email do usuário não encontrado no token.", "", 5000, false);
        return;
    }
    console.log("Token:", token);
    console.log("Payload do Token:", payload);
    fetch(`http://localhost:8080/api/usuario/buscar/email?email=${emailUsuario}`, {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + token
        }
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(errorData => {
                throw new Error(errorData.error || "Erro ao buscar informações do usuário.");
            });
        }
        return response.json();
    })
    .then(data => {
        if (data.error) {
            throw new Error(data.error);
        }
        const nomeUsuario = data.nomeUser; 
        const empresaUsuario = data.empresa; 
    
        localStorage.setItem("nomeUsuario", nomeUsuario);
        localStorage.setItem("empresaUsuario", empresaUsuario);

        carregarResultados();
    })
    .catch(error => {
        console.error("Erro ao obter informações do usuário:", error);
        showError(error.message || "Erro ao carregar informações do usuário.", "", 5000, false);
    });

    function carregarResultados() {
        const resultadosString = localStorage.getItem("resultadosPesquisa");
        let resultados = [];

        try {
            if (resultadosString && resultadosString !== "undefined") {
                resultados = JSON.parse(resultadosString);
            } else {
                console.warn("Nenhum dado encontrado no localStorage.");
            }
        } catch (error) {
            console.error("Erro ao analisar resultados da pesquisa:", error);
            showError("Erro ao carregar os resultados da pesquisa.", "", 5000, false);
        }

        const tbody = document.querySelector("#resultadosTable tbody");

        if (resultados && resultados.length > 0) {
            resultados.forEach(produto => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${produto.nomeProduto}</td>
                    <td>${produto.codBarras}</td>
                    <td>${produto.dataContagem}</td>
                    <td>${produto.dataValidade}</td>
                    <td>${produto.quantidadeContada}</td>
                    <td>${produto.funcionario.nomeUser}</td>
                `;
                tbody.appendChild(row);
            });
        } else {
            const row = document.createElement("tr");
            row.innerHTML = `<td colspan="8">Nenhum produto encontrado.</td>`;
            tbody.appendChild(row);
        }

        document.getElementById("exportPdfButton").addEventListener("click", function () {
            exportPDF();
            localStorage.removeItem("resultadosPesquisa"); 
        });

        document.getElementById("exportExeButton").addEventListener("click", function () {
            exportXLS(); 
            localStorage.removeItem("resultadosPesquisa"); 
        });
    }
});