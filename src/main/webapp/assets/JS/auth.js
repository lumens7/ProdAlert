function checkAuth() {
    const token = localStorage.getItem("jwt");

    if (!token) {
        showError("Você precisa estar logado para acessar esta página.", "/api/usuario/login.html", 5000, true);
        return false;
    }

    const payload = JSON.parse(atob(token.split('.')[1])); 
    const expirationDate = new Date(payload.exp * 1000); 
    if (expirationDate < new Date()) {
        localStorage.removeItem("jwt");
        showError("Sua sessão expirou. Faça login novamente.", "/api/usuario/login.html", 5000, true);
        return false;
    }

    return true;
}

function checkRole(requiredRole) {
    const roles = JSON.parse(localStorage.getItem("roles"));

    if (!roles || !roles.includes(requiredRole)) {
        showError("Você não tem permissão para acessar esta página.", "/api/usuario/login.html", 5000, true);
        return false;
    }
    return true;
}
document.addEventListener("DOMContentLoaded", function () {
    if (!checkAuth()) {
        return; 
    }

    if (!checkRole("FUNCIONARIO")) { 
        return; 
    }
});