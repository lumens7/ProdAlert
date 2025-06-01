function showSuccess(message, redirectUrl = null, autoRedirectTime = 20000) { // Padrão 20 segundos
    const successOverlay = document.getElementById("successOverlay");
    const successMessage = document.getElementById("successMessage");
    const closeSuccessButton = document.getElementById("closeSuccessButton");
    const countdownTimer = document.getElementById("countdownTimer");

    if (successOverlay && successMessage && closeSuccessButton && countdownTimer) {
        successMessage.textContent = message;
        successOverlay.style.display = "flex";

        // Se não houver redirecionamento, esconde o contador
        if (!redirectUrl) {
            countdownTimer.style.display = "none";
            return;
        }

        // Mostra o contador apenas se houver redirecionamento
        countdownTimer.style.display = "block";
        let timeRemaining = Math.floor(autoRedirectTime / 1000);
        countdownTimer.textContent = `Redirecionando em ${timeRemaining}s`;

        const countdownInterval = setInterval(() => {
            timeRemaining--;
            countdownTimer.textContent = `Redirecionando em ${timeRemaining}s`;

            if (timeRemaining <= 0) {
                clearInterval(countdownInterval);
                window.location.href = redirectUrl;
            }
        }, 1000);

        closeSuccessButton.addEventListener("click", function() {
            clearInterval(countdownInterval);
            successOverlay.style.display = "none";
            window.location.href = redirectUrl;
        });
    } else {
        console.error("Elementos da mensagem de sucesso não encontrados.");
    }
}