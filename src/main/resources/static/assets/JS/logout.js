function logout() {
    // Remove o token JWT e outros dados do usuário do localStorage
    localStorage.removeItem("jwt");
    localStorage.removeItem("username");
    localStorage.removeItem("roles");

    // Redireciona para a página de login
    window.location.href = "/src/main/resources/static/api/usuario/login.html";
}

// Adiciona o evento de clique ao botão de logoff
document.addEventListener("DOMContentLoaded", function () {
    const logoutButton = document.getElementById("logoutButton");
    if (logoutButton) {
        logoutButton.addEventListener("click", logout);
    } else {
        console.error("Botão de logoff não encontrado.");
    }
});