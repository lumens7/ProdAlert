document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const email = urlParams.get('email');
    
    if (!email) {
        window.location.href = '/src/main/resources/static/api/usuario/signup.html';
        return;
    }
    
    document.getElementById('email-display').textContent = email;
    document.getElementById('user-email').value = email;
    
    // Configuração dos inputs de código
    const codeInputs = document.querySelectorAll('.code-input');
    const fullCodeInput = document.getElementById('full-code');
    
    codeInputs.forEach((input, index) => {
        input.addEventListener('input', function() {
            if (this.value.length === 1) {
                if (index < codeInputs.length - 1) {
                    codeInputs[index + 1].focus();
                }
            }
            
            updateFullCode();
        });
        
        input.addEventListener('keydown', function(e) {
            if (e.key === 'Backspace' && this.value.length === 0 && index > 0) {
                codeInputs[index - 1].focus();
            }
        });
    });
    
    function updateFullCode() {
        let code = '';
        codeInputs.forEach(input => {
            code += input.value;
        });
        fullCodeInput.value = code;
    }
    
    // Configuração do timer
    let timeLeft = 300; // 5 minutos em segundos
    const timerElement = document.getElementById('timer');
    
    function updateTimer() {
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        
        timerElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        
        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            document.getElementById('verify-button').disabled = true;
            timerElement.textContent = "Código expirado";
            timerElement.style.color = "red";
        }
        
        timeLeft--;
    }
    
    const timerInterval = setInterval(updateTimer, 1000);
    updateTimer();
    
    // Envio do formulário de verificação
    document.getElementById('verificationForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const code = document.getElementById('full-code').value;
        const email = document.getElementById('user-email').value;

        try {
            const response = await fetch("http://localhost:8080/api/usuario/complete-signup", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    email: email,
                    code: code
                })
            });
            
            if (response.ok) {
                const userData = await response.json();
                showSuccess("Cadastro realizado com sucesso! Redirecionando para login...");
                
                // Limpa dados temporários
                sessionStorage.removeItem('pendingEmail');
                
                // Redireciona após 3 segundos
                setTimeout(() => {
                    window.location.href = `login.html?email=${encodeURIComponent(email)}`;
                }, 20000);
            } else {
                const error = await response.text();
                showError(error);
            }
        } catch (error) {
            showError("Erro ao conectar com o servidor.");
        }
    });
    
    // Reenvio do código
    document.getElementById('resend-button').addEventListener('click', async function() {
        try {
            const response = await fetch("http://localhost:8080/api/usuario/send-verification-code?email=" + encodeURIComponent(email), {
                method: "POST"
            });
            
            if (response.ok) {
                // Reinicia o timer
                timeLeft = 300;
                document.getElementById('verify-button').disabled = false;
                timerElement.style.color = "";
                clearInterval(timerInterval);
                timerInterval = setInterval(updateTimer, 1000);
                updateTimer();
                
                // Limpa os inputs
                codeInputs.forEach(input => input.value = '');
                fullCodeInput.value = '';
                codeInputs[0].focus();
                
                showSuccess("Código reenviado com sucesso!");
            } else {
                const error = await response.text();
                showError(error || "Erro ao reenviar código.");
            }
        } catch (error) {
            console.error("Erro ao reenviar código:", error);
            showError("Erro ao conectar com o servidor.");
        }
    });
});

function showSuccess(message) {
    alert(message); // Você pode substituir por sua implementação de mensagem de sucesso
}

function showError(message) {
    const errorOverlay = document.getElementById('errorOverlay');
    const errorMessage = document.getElementById('errorMessage');
    
    errorMessage.textContent = message;
    errorOverlay.style.display = 'flex';
    
    document.getElementById('closeErrorButton').onclick = function() {
        errorOverlay.style.display = 'none';
    };
}