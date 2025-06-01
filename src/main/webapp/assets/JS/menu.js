document.addEventListener("DOMContentLoaded", function () {
    if (!checkAuth()) {
        return; 
    }
    const token = localStorage.getItem("jwt");
    if (!token) {
        showError("Token JWT não encontrado.", "/api/usuario/login.html", 0, true);
        return;
    }
    try {
        const payload = JSON.parse(atob(token.split('.')[1])); 
        console.log("Payload do JWT:", payload); 
        const userRoles = payload.roles; 
        console.log("ROLES do usuário:", userRoles); 
        if (!userRoles || !Array.isArray(userRoles)) {
            throw new Error("ROLES do usuário não encontradas ou em formato inválido.");
        }
        const menuContainer = document.createElement("div");
        menuContainer.className = "menuH";
        if (window.innerWidth > 768) {
            const logoLink = document.createElement("a");
            logoLink.href = "/api/produto/home.html";
            const logoImg = document.createElement("img");
            logoImg.src = "/assets/fotos/logo.png";
            logoImg.className = "logo";
            logoImg.alt = "";
            logoLink.appendChild(logoImg);
            menuContainer.appendChild(logoLink);
        }
        const homeLink = createMenuItem("/api/produto/home.html", "home.svg", "Inicio");
        menuContainer.appendChild(homeLink);

        const cadastroLink = createMenuItem("/api/produto/cadastro.html", "box.svg", "Cadastro Produtos");
        menuContainer.appendChild(cadastroLink);

        const pesquisaLink = createMenuItem("/api/produto/pesquisa.html", "lupa.svg", "Pesquisa Produtos");
        menuContainer.appendChild(pesquisaLink);

        const dadosUsuarioLink = document.createElement("a");
        dadosUsuarioLink.href = userRoles.includes("ROLE_FUNCIONARIO") 
            ? "/api/usuario/dados_user.html" 
            : "/api/usuario/dados_empresa.html";
        dadosUsuarioLink.className = "menu-item";
        const dadosUsuarioIcon = document.createElement("img");
        dadosUsuarioIcon.src = "/assets/fotos/svg/user.svg";
        dadosUsuarioIcon.className = "menu-icon";
        dadosUsuarioIcon.alt = "Dados do Usuário";
        const dadosUsuarioText = document.createElement("span");
        dadosUsuarioText.className = "menu-text";
        dadosUsuarioText.textContent = "Dados do Usuário";
        dadosUsuarioLink.appendChild(dadosUsuarioIcon);
        dadosUsuarioLink.appendChild(dadosUsuarioText);
        menuContainer.appendChild(dadosUsuarioLink);

        if (userRoles.includes("ROLE_EMPRESA") || userRoles.includes("ROLE_ADMIN")) {
            const pesquisaFuncionarioLink = createMenuItem("/api/usuario/pesquisa_user.html", "users.svg", "Pesquisa Funcionário");
            menuContainer.appendChild(pesquisaFuncionarioLink);

            const vincularFuncionarioLink = createMenuItem("/api/usuario/vincular.html", "vinc.svg", "Vincular Funcionário");
            menuContainer.appendChild(vincularFuncionarioLink);

            const inativarFuncionarioLink = createMenuItem("/api/usuario/inativar.html", "userRM.svg", "Inativar Funcionário");
            menuContainer.appendChild(inativarFuncionarioLink);
        }
        document.body.insertBefore(menuContainer, document.body.firstChild);
        window.addEventListener('resize', function() {
            const logo = document.querySelector('.logo');
            if (window.innerWidth <= 768 && logo) {
                logo.parentElement.remove();
            } else if (window.innerWidth > 768 && !document.querySelector('.logo')) {
                const logoLink = document.createElement("a");
                logoLink.href = "/api/produto/home.html";
                const logoImg = document.createElement("img");
                logoImg.src = "/assets/fotos/logo.png";
                logoImg.className = "logo";
                logoImg.alt = "";
                logoLink.appendChild(logoImg);
                menuContainer.insertBefore(logoLink, menuContainer.firstChild);
            }
        });
    } catch (error) {
        console.error("Erro ao ajustar o menu:", error);
        showError("Erro ao carregar o menu: " + error.message, null, 0, false);
    }
});
function createMenuItem(href, iconSrc, text) {
    const menuItem = document.createElement("a");
    menuItem.href = href;
    menuItem.className = "menu-item";

    const menuIcon = document.createElement("img");
    menuIcon.src = `/assets/fotos/svg/${iconSrc}`;
    menuIcon.className = "menu-icon";
    menuIcon.alt = text;

    const menuText = document.createElement("span");
    menuText.className = "menu-text";
    menuText.textContent = text;

    menuItem.appendChild(menuIcon);
    menuItem.appendChild(menuText);

    return menuItem;
}