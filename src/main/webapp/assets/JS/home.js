document.addEventListener("DOMContentLoaded", function () {
    if (!checkAuth()) {
        return;
    }
    
    fetch(`http://localhost:8080/api/produto/buscar/ultimos6`, {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("jwt")
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Erro ao buscar produtos.");
        }
        return response.json();
    })
    .then(data => {
        const produtosContainer = document.getElementById("produtos-container");
        if (data.length === 0) {
            produtosContainer.innerHTML = "<p>Nenhum produto cadastrado recentemente.</p>";
            return;
        }

        data.forEach(produto => {
            const card = document.createElement("div");
            card.className = "card";
            card.innerHTML = `
                <h3>${produto.nomeProduto.split(" ")
                    .map(palavra => palavra.charAt(0)
                    .toUpperCase() + palavra.slice(1))
                    .join(" ")}</h3>
                <p><strong>Código de Barras:</strong><p> ${produto.codBarras}</p>
                <p><strong>Data de Validade:</strong><p> ${new Date(produto.dataValidade).toLocaleDateString()}</p>
                <p><strong>Cadastrado por:</strong><p> ${produto.funcionario.nomeUser}</p>
            `;
            produtosContainer.appendChild(card);
        });
    })
    .catch(error => {
        showError("Erro ao buscar produtos: " + error.message, null, 0, false);
    });
});