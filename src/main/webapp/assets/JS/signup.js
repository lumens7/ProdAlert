function aplicarMascaraCPF(cpf) {
    cpf = cpf.replace(/\D/g, '');
    cpf = cpf.replace(/(\d{3})(\d)/, '$1.$2'); 
    cpf = cpf.replace(/(\d{3})(\d)/, '$1.$2');
    cpf = cpf.replace(/(\d{3})(\d{1,2})$/, '$1-$2'); 
    return cpf;
}
function aplicarMascaraCNPJ(cnpj) {
    cnpj = cnpj.replace(/\D/g, '');
    cnpj = cnpj.replace(/^(\d{2})(\d)/, '$1.$2'); 
    cnpj = cnpj.replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3');
    cnpj = cnpj.replace(/\.(\d{3})(\d)/, '.$1/$2'); 
    cnpj = cnpj.replace(/(\d{4})(\d)/, '$1-$2'); 
    return cnpj;
}
document.getElementById("cpf").addEventListener("input", function (e) {
    e.target.value = aplicarMascaraCPF(e.target.value);
});
document.getElementById("cnpj").addEventListener("input", function (e) {
    e.target.value = aplicarMascaraCNPJ(e.target.value);
});
document.addEventListener("DOMContentLoaded", function () {
    const pessoaFisicaRadio = document.getElementById("pessoaFisica");
    const pessoaJuridicaRadio = document.getElementById("pessoaJuridica");
    const pessoaFisicaFields = document.getElementById("pessoaFisicaFields");
    const pessoaJuridicaFields = document.getElementById("pessoaJuridicaFields");
    const cpfField = document.getElementById("cpf");
    const cnpjField = document.getElementById("cnpj");
    const nomeField = document.getElementById("nome");
    const razaoSocialField = document.getElementById("razaoSocial");
    function toggleFields() {
        if (pessoaFisicaRadio.checked) {
            pessoaFisicaFields.style.display = "block";
            pessoaJuridicaFields.style.display = "none";
            cpfField.setAttribute("required", true);
            nomeField.setAttribute("required", true);
            cnpjField.removeAttribute("required");
            razaoSocialField.removeAttribute("required");
        } else {
            pessoaFisicaFields.style.display = "none";
            pessoaJuridicaFields.style.display = "block";
            cnpjField.setAttribute("required", true);
            razaoSocialField.setAttribute("required", true);
            cpfField.removeAttribute("required");
            nomeField.removeAttribute("required");
        }
    }
    pessoaFisicaRadio.addEventListener("change", toggleFields);
    pessoaJuridicaRadio.addEventListener("change", toggleFields);
    toggleFields();
});
document.getElementById("cadastroForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const pessoaFisicaRadio = document.getElementById("pessoaFisica");
    const isPessoaFisica = pessoaFisicaRadio.checked;
    let cpf = isPessoaFisica ? document.getElementById("cpf").value.trim() : null;
    let cnpj = !isPessoaFisica ? document.getElementById("cnpj").value.trim() : null;
    const nome = isPessoaFisica ? document.getElementById("nome").value.trim() : document.getElementById("razaoSocial").value.trim();
    const email = document.getElementById("email").value.trim();
    const senha = document.getElementById("senha").value.trim();
    const confirmarSenha = document.getElementById("confirmarSenha").value.trim();
    if (cpf) cpf = cpf.replace(/\D/g, '');
    if (cnpj) cnpj = cnpj.replace(/\D/g, '');
    const cpfRegex = /^\d{11}$/;
    const cnpjRegex = /^\d{14}$/;

    if (isPessoaFisica && !cpfRegex.test(cpf)) {
        showError("CPF deve ter 11 dígitos.");
        return;
    } else if (!isPessoaFisica && !cnpjRegex.test(cnpj)) {
        showError("CNPJ deve ter 14 dígitos.");
        return;
    }

    if (senha.length < 8) {
        showError("A senha deve ter pelo menos 8 caracteres.");
        return;
    }
    try {
        const response = await fetch("http://localhost:8080/api/usuario/init-signup", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                nomeUser: nome,
                mail: email,
                senha: senha,
                confirmarSenha: confirmarSenha,
                cpfcnpj: isPessoaFisica ? cpf : cnpj
            })
        });

        if (response.ok) {
            sessionStorage.setItem('pendingEmail', email);
            window.location.href = `verification.html?email=${encodeURIComponent(email)}`;
        } else {
            const error = await response.text();
            showError(error);
        }
    } catch (error) {
        showError("Erro ao conectar com o servidor.");
    }
});