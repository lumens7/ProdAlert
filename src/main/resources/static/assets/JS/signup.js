function aplicarMascaraCPF(cpf) {
    cpf = cpf.replace(/\D/g, ''); // Remove tudo que não é dígito
    cpf = cpf.replace(/(\d{3})(\d)/, '$1.$2'); // Coloca um ponto após os 3 primeiros dígitos
    cpf = cpf.replace(/(\d{3})(\d)/, '$1.$2'); // Coloca um ponto após os 6 primeiros dígitos
    cpf = cpf.replace(/(\d{3})(\d{1,2})$/, '$1-$2'); // Coloca um traço antes dos 2 últimos dígitos
    return cpf;
}

// Função para aplicar máscara de CNPJ
function aplicarMascaraCNPJ(cnpj) {
    cnpj = cnpj.replace(/\D/g, ''); // Remove tudo que não é dígito
    cnpj = cnpj.replace(/^(\d{2})(\d)/, '$1.$2'); // Coloca um ponto após os 2 primeiros dígitos
    cnpj = cnpj.replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3'); // Coloca um ponto após os 5 primeiros dígitos
    cnpj = cnpj.replace(/\.(\d{3})(\d)/, '.$1/$2'); // Coloca uma barra após os 8 primeiros dígitos
    cnpj = cnpj.replace(/(\d{4})(\d)/, '$1-$2'); // Coloca um traço antes dos 2 últimos dígitos
    return cnpj;
}

// Aplicar máscara ao campo de CPF
document.getElementById("cpf").addEventListener("input", function (e) {
    e.target.value = aplicarMascaraCPF(e.target.value);
});

// Aplicar máscara ao campo de CNPJ
document.getElementById("cnpj").addEventListener("input", function (e) {
    e.target.value = aplicarMascaraCNPJ(e.target.value);
});

// Configuração do formulário
document.addEventListener("DOMContentLoaded", function () {
    const pessoaFisicaRadio = document.getElementById("pessoaFisica");
    const pessoaJuridicaRadio = document.getElementById("pessoaJuridica");
    const pessoaFisicaFields = document.getElementById("pessoaFisicaFields");
    const pessoaJuridicaFields = document.getElementById("pessoaJuridicaFields");
    const cpfField = document.getElementById("cpf");
    const cnpjField = document.getElementById("cnpj");
    const nomeField = document.getElementById("nome");
    const razaoSocialField = document.getElementById("razaoSocial");

    // Função para alternar os campos e gerenciar o atributo "required"
    function toggleFields() {
        if (pessoaFisicaRadio.checked) {
            pessoaFisicaFields.style.display = "block";
            pessoaJuridicaFields.style.display = "none";

            // Adiciona "required" aos campos de Pessoa Física
            cpfField.setAttribute("required", true);
            nomeField.setAttribute("required", true);

            // Remove "required" dos campos de Pessoa Jurídica
            cnpjField.removeAttribute("required");
            razaoSocialField.removeAttribute("required");
        } else {
            pessoaFisicaFields.style.display = "none";
            pessoaJuridicaFields.style.display = "block";

            // Adiciona "required" aos campos de Pessoa Jurídica
            cnpjField.setAttribute("required", true);
            razaoSocialField.setAttribute("required", true);

            // Remove "required" dos campos de Pessoa Física
            cpfField.removeAttribute("required");
            nomeField.removeAttribute("required");
        }
    }

    // Adiciona listeners para os inputs de rádio
    pessoaFisicaRadio.addEventListener("change", toggleFields);
    pessoaJuridicaRadio.addEventListener("change", toggleFields);

    // Inicializa os campos corretamente ao carregar a página
    toggleFields();
});

// Lógica de envio do formulário
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

    // Remover formatação do CPF e CNPJ
    if (cpf) cpf = cpf.replace(/\D/g, '');
    if (cnpj) cnpj = cnpj.replace(/\D/g, '');

    // Validação de CPF e CNPJ
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
            // Salva email no sessionStorage para uso na página de verificação
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