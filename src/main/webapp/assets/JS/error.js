function showError(message, redirectUrl = null, autoRedirectTime = 5000, shouldRedirect = true) {
    console.log("showError chamada com a mensagem:", message);
    const errorOverlay = document.getElementById("errorOverlay");
    const errorMessage = document.getElementById("errorMessage");
    if (errorOverlay && errorMessage) {
        errorMessage.textContent = message;
        errorOverlay.style.display = "flex"; 
        if (redirectUrl && shouldRedirect) {
            let timeRemaining = autoRedirectTime / 1000;
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
document.addEventListener("DOMContentLoaded", function () {
    const closeErrorButton = document.getElementById("closeErrorButton");
    if (closeErrorButton) {
        closeErrorButton.addEventListener("click", function () {
            const errorOverlay = document.getElementById("errorOverlay");
            if (errorOverlay) {
                errorOverlay.style.display = "none";
                const redirectUrl = "/api/usuario/login.html";
                const shouldRedirect = errorOverlay.getAttribute("data-should-redirect") === "true";
                if (shouldRedirect) {
                    window.location.href = redirectUrl;
                }
            }
        });
    } else {
        console.error("Botão de fechar erro não encontrado.");
    }
});