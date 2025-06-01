document.getElementById("btnPesquisar").addEventListener("click", async function (event) {
    event.preventDefault();
    const nomeProduto = document.getElementById("nomeProduto").value.trim();
    const codBarras = document.getElementById("codBarras").value.trim();
    const dataContagemInicial = document.getElementById("dataContagemInicial").value.trim();
    const dataContagemFinal = document.getElementById("dataContagemFinal").value.trim();
    const dataValidadeInicial = document.getElementById("dataValidadeInicial").value.trim();
    const dataValidadeFinal = document.getElementById("dataValidadeFinal").value.trim();
    try {
        let url;
        const params = new URLSearchParams();
    if (nomeProduto && dataContagemInicial && dataContagemFinal) {
        url = "http://localhost:8080/api/produto/buscar/NomeProdutoeDataContagem";
        params.append("nomeProduto", nomeProduto);
        params.append("dataContagemInicial", formatDateToBackend(dataContagemInicial));
        params.append("dataContagemFinal", formatDateToBackend(dataContagemFinal));
    } 
    else if (nomeProduto && dataValidadeInicial && dataValidadeFinal) {
        url = "http://localhost:8080/api/produto/buscar/NomeProdutoeDataValidade";
        params.append("nomeProduto", nomeProduto);
        params.append("dataValidadeInicial", formatDateToBackend(dataValidadeInicial));
        params.append("dataValidadeFinal", formatDateToBackend(dataValidadeFinal));
    }
        else if (nomeProduto) {
            url = "http://localhost:8080/api/produto/buscar/nome";
            params.append("nome", nomeProduto);
        } 
        else if (codBarras) {
            url = "http://localhost:8080/api/produto/buscar/codigo";
            params.append("codBarras", codBarras);
        } 
        else if (dataContagemInicial && dataContagemFinal) {
            url = "http://localhost:8080/api/produto/buscar/intervaloDataContagem";
            params.append("dataContagemInicial", formatDateToBackend(dataContagemInicial));
            params.append("dataContagemFinal", formatDateToBackend(dataContagemFinal));
        } 
        else if (dataValidadeInicial && dataValidadeFinal) {
            url = "http://localhost:8080/api/produto/buscar/intervaloDataValidade";
            params.append("dataValidadeInicial", formatDateToBackend(dataValidadeInicial));
            params.append("dataValidadeFinal", formatDateToBackend(dataValidadeFinal));
        } 
        else {
            url = "http://localhost:8080/api/produto/buscar/Ativos";
        }
        const response = await fetch(url + (params.toString() ? `?${params.toString()}` : ''), {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + localStorage.getItem("jwt")
            }
        });
        if (response.ok) {
            const produtos = await response.json();
            if (produtos.length === 0) {
                showError("Nenhum produto encontrado com os critérios informados.");
            } else {
                localStorage.setItem("resultadosPesquisa", JSON.stringify(produtos));
                window.location.href = "/api/produto/ReturnPesquisa.html";
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
function formatDateToBackend(dateString) {
    const [day, month, year] = dateString.split('/');
    return `${day}${month}${year}`;
}
function showError(message) {
    const errorOverlay = document.getElementById("errorOverlay");
    const errorMessage = document.getElementById("errorMessage");
    errorMessage.textContent = message;
    errorOverlay.style.display = "flex";

    document.getElementById("closeErrorButton").addEventListener("click", function () {
        errorOverlay.style.display = "none";
    });
}