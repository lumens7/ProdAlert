document.getElementById("btnPesquisar").addEventListener("click", async function (event) {
    event.preventDefault();

    // Captura os valores dos campos de pesquisa
    const nomeProduto = document.getElementById("nomeProduto").value.trim();
    const codBarras = document.getElementById("codBarras").value.trim();
    const dataContagemInicial = document.getElementById("dataContagemInicial").value.trim();
    const dataContagemFinal = document.getElementById("dataContagemFinal").value.trim();
    const dataValidadeInicial = document.getElementById("dataValidadeInicial").value.trim();
    const dataValidadeFinal = document.getElementById("dataValidadeFinal").value.trim();

    try {
        let url;
        const params = new URLSearchParams();
        
        // Verifica combinações específicas primeiro
        // Dentro do event listener do botão de pesquisa:
    if (nomeProduto && dataContagemInicial && dataContagemFinal) {
        // Pesquisa por nome e intervalo de data de contagem
        url = "http://localhost:8080/api/produto/buscar/NomeProdutoeDataContagem";
        params.append("nomeProduto", nomeProduto);
        params.append("dataContagemInicial", formatDateToBackend(dataContagemInicial));
        params.append("dataContagemFinal", formatDateToBackend(dataContagemFinal));
    } 
    else if (nomeProduto && dataValidadeInicial && dataValidadeFinal) {
        // Pesquisa por nome e intervalo de data de validade
        url = "http://localhost:8080/api/produto/buscar/NomeProdutoeDataValidade";
        params.append("nomeProduto", nomeProduto);
        params.append("dataValidadeInicial", formatDateToBackend(dataValidadeInicial));
        params.append("dataValidadeFinal", formatDateToBackend(dataValidadeFinal));
    }
        else if (nomeProduto) {
            // Pesquisa apenas por nome
            url = "http://localhost:8080/api/produto/buscar/nome";
            params.append("nome", nomeProduto);
        } 
        else if (codBarras) {
            // Pesquisa por código de barras
            url = "http://localhost:8080/api/produto/buscar/codigo";
            params.append("codBarras", codBarras);
        } 
        else if (dataContagemInicial && dataContagemFinal) {
            // Pesquisa por intervalo de data de contagem
            url = "http://localhost:8080/api/produto/buscar/intervaloDataContagem";
            params.append("dataContagemInicial", formatDateToBackend(dataContagemInicial));
            params.append("dataContagemFinal", formatDateToBackend(dataContagemFinal));
        } 
        else if (dataValidadeInicial && dataValidadeFinal) {
            // Pesquisa por intervalo de data de validade
            url = "http://localhost:8080/api/produto/buscar/intervaloDataValidade";
            params.append("dataValidadeInicial", formatDateToBackend(dataValidadeInicial));
            params.append("dataValidadeFinal", formatDateToBackend(dataValidadeFinal));
        } 
        else {
            // Se nenhum campo foi preenchido, busca todos os produtos ativos
            url = "http://localhost:8080/api/produto/buscar/Ativos";
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
            const produtos = await response.json();
            if (produtos.length === 0) {
                showError("Nenhum produto encontrado com os critérios informados.");
            } else {
                localStorage.setItem("resultadosPesquisa", JSON.stringify(produtos));
                window.location.href = "/src/main/resources/static/api/produto/ReturnPesquisa.html";
            }
        } else {
            const errorData = await response.json().catch(() => null);
            const errorMessage = errorData?.message || "Erro ao realizar a pesquisa.";
            showError(errorMessage);
        }
    } catch (error) {
        showError("Erro ao conectar com o servidor: " + error.message);
    }
});

// Função para formatar a data de DD/MM/AAAA para ddMMyyyy
function formatDateToBackend(dateString) {
    const [day, month, year] = dateString.split('/');
    return `${day}${month}${year}`;
}

// Função para exibir mensagens de erro
function showError(message) {
    const errorOverlay = document.getElementById("errorOverlay");
    const errorMessage = document.getElementById("errorMessage");
    errorMessage.textContent = message;
    errorOverlay.style.display = "flex";

    document.getElementById("closeErrorButton").addEventListener("click", function () {
        errorOverlay.style.display = "none";
    });
}