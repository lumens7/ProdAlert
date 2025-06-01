document.getElementById("loginForm").addEventListener("submit", async function(event) {
    event.preventDefault();
    const spinner = document.getElementById("loadingSpinner");
    const submitButton = event.target.querySelector("button[type='submit']");
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    spinner.style.display = "flex";
    submitButton.disabled = true;
    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 10000); 
        const response = await fetch("http://localhost:8080/api/usuario/login", {
            method: "POST",
            headers: { 
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify({ mail: email, senha: password }),
            signal: controller.signal
        });
        clearTimeout(timeoutId);
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Erro ${response.status}: ${response.statusText}`);
        }
        const data = await response.json();
        localStorage.setItem("jwt", data.token);
        localStorage.setItem("username", data.username);
        localStorage.setItem("roles", JSON.stringify(data.roles));
        
        window.location.href = "/api/produto/home.html";
        
    } catch (error) {
        console.error("Erro no login:", error);
        showError(error.message.includes("Invalid credentials") 
            ? "E-mail ou senha incorretos" 
            : error.message || "Erro ao tentar fazer login");
    } finally {
        spinner.style.display = "none";
        submitButton.disabled = false;
    }
});