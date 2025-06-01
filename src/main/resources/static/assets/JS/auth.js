function checkAuth() {
    const token = localStorage.getItem("jwt");

    if (!token) {
        showError("Você precisa estar logado para acessar esta página.", "/src/main/resources/static/api/usuario/login.html", 5000, true);
        return false;
    }

    // Verifica se o token está expirado (opcional)
    const payload = JSON.parse(atob(token.split('.')[1])); // Decodifica o payload do token
    const expirationDate = new Date(payload.exp * 1000); // Converte o timestamp para milissegundos
    if (expirationDate < new Date()) {
        localStorage.removeItem("jwt"); // Remove o token expirado
        showError("Sua sessão expirou. Faça login novamente.", "/src/main/resources/static/api/usuario/login.html", 5000, true);
        return false;
    }

    return true;
}

function checkRole(requiredRole) {
    const roles = JSON.parse(localStorage.getItem("roles"));

    if (!roles || !roles.includes(requiredRole)) {
        showError("Você não tem permissão para acessar esta página.", "/src/main/resources/static/api/usuario/login.html", 5000, true);
        return false;
    }

    return true;
}

// Verifica a autenticação e permissões ao carregar a página
document.addEventListener("DOMContentLoaded", function () {
    if (!checkAuth()) {
        return; // Interrompe a execução se o usuário não estiver autenticado
    }

    // Verifica a role necessária para a página (opcional)
    if (!checkRole("FUNCIONARIO")) { // Substitua pela role necessária
        return; // Interrompe a execução se o usuário não tiver a role necessária
    }
});