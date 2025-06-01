function showError(message, redirectUrl = null, autoRedirectTime = 5000, shouldRedirect = true) {
    console.log("showError chamada com a mensagem:", message); // Depuração
    const errorOverlay = document.getElementById("errorOverlay");
    const errorMessage = document.getElementById("errorMessage");

    if (errorOverlay && errorMessage) {
        errorMessage.textContent = message; // Define a mensagem de erro
        errorOverlay.style.display = "flex"; // Exibe a sobreposição

        // Redireciona após um tempo, se uma URL for fornecida e o redirecionamento estiver habilitado
        if (redirectUrl && shouldRedirect) {
            let timeRemaining = autoRedirectTime / 1000; // Convertendo de milissegundos para segundos
            const countdownTimer = document.createElement("p");
            countdownTimer.textContent = `Redirecionando em ${timeRemaining}s`;
            errorOverlay.appendChild(countdownTimer);

            const countdownInterval = setInterval(() => {
                timeRemaining--;
                countdownTimer.textContent = `Redirecionando em ${timeRemaining}s`;

                if (timeRemaining <= 0) {
                    clearInterval(countdownInterval);
                    window.location.href = redirectUrl;
                }
            }, 1000);
        }
    } else {
        console.error("Elementos da mensagem de erro não encontrados.");
    }
}

// Fechar a mensagem de erro ao clicar no botão "X"
document.addEventListener("DOMContentLoaded", function () {
    const closeErrorButton = document.getElementById("closeErrorButton");
    if (closeErrorButton) {
        closeErrorButton.addEventListener("click", function () {
            const errorOverlay = document.getElementById("errorOverlay");
            if (errorOverlay) {
                errorOverlay.style.display = "none"; // Oculta a sobreposição

                // Redireciona apenas se o redirecionamento estiver habilitado
                const redirectUrl = "/src/main/resources/static/api/usuario/login.html"; // URL de redirecionamento
                const shouldRedirect = errorOverlay.getAttribute("data-should-redirect") === "true";
                if (shouldRedirect) {
                    window.location.href = redirectUrl; // Redireciona imediatamente
                }
            }
        });
    } else {
        console.error("Botão de fechar erro não encontrado.");
    }
});