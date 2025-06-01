function logout() {
    localStorage.removeItem("jwt");
    localStorage.removeItem("username");
    localStorage.removeItem("roles");

    window.location.href = "/api/usuario/login.html";
}

document.addEventListener("DOMContentLoaded", function () {
    const logoutButton = document.getElementById("logoutButton");
    if (logoutButton) {
        logoutButton.addEventListener("click", logout);
    } else {
        console.error("Botão de logoff não encontrado.");
    }
});