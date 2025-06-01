document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem("jwt");
    if (!token) {
        showError("Você precisa estar logado para acessar esta página.", "/api/usuario/login.html", 5000, true);
        return;
    }
    carregarResultados();
    function carregarResultados() {
        const resultadosString = localStorage.getItem("resultadosPesquisaUsuarios");
        let resultados = [];
        try {
            if (resultadosString && resultadosString !== "undefined") {
                resultados = JSON.parse(resultadosString);
            } else {
                console.warn("Nenhum dado encontrado no localStorage.");
                showError("Nenhum resultado de pesquisa encontrado.", "", 5000, false);
                return;
            }
        } catch (error) {
            console.error("Erro ao analisar resultados da pesquisa:", error);
            showError("Erro ao carregar os resultados da pesquisa.", "", 5000, false);
            return;
        }
        const tbody = document.querySelector("#resultadosTable tbody");
        if (resultados && resultados.length > 0) {
            resultados.forEach(usuario => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${usuario.nomeUser || 'N/A'}</td>
                    <td>${usuario.mail || 'N/A'}</td>
                    <td>${formatCPF(usuario.cpf) || 'N/A'}</td>
                `;
                tbody.appendChild(row);
            });
        } else {
            const row = document.createElement("tr");
            row.innerHTML = `<td colspan="6">Nenhum usuário encontrado.</td>`;
            tbody.appendChild(row);
        }
        document.getElementById("exportPdfButton").addEventListener("click", function () {
            exportPDF(); 
            localStorage.removeItem("resultadosPesquisaUsuarios"); 
        });
        document.getElementById("exportExeButton").addEventListener("click", function () {
            exportXLS(); 
            localStorage.removeItem("resultadosPesquisaUsuarios");
        });
    }
    function formatCPF(cpf) {
        if (!cpf) return 'N/A';
        return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    }
});