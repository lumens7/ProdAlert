document.addEventListener('DOMContentLoaded', function() {
    const solicitarCodigoForm = document.getElementById('solicitarCodigoForm');
    const redefinirSenhaForm = document.getElementById('redefinirSenhaForm');
    const reenviarCodigo = document.getElementById('reenviarCodigo');
    const passo1 = document.getElementById('passo1');
    const passo2 = document.getElementById('passo2');
    const emailInput = document.getElementById('email');
    const emailConfirmacao = document.getElementById('emailConfirmacao');
    solicitarCodigoForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        const email = emailInput.value.trim();
        if (!email) {
            showError('Por favor, informe seu e-mail');
            return;
        }
        try {
            const response = await fetch('http://localhost:8080/api/usuario/alterarSenha', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ mail: email })
            });
            const responseData = await response.text();
            if (response.ok) {
                passo1.style.display = 'none';
                passo2.style.display = 'block';
                emailConfirmacao.value = email;
                showSuccess('Código enviado para o seu e-mail', null, 3000);
            } else {
                if (response.status === 404) {
                    showError('E-mail não cadastrado no sistema');
                } else {
                    showError(responseData || 'Erro ao enviar código de verificação');
                }
            }
        } catch (error) {
            console.error('Erro:', error);
            showError('Erro ao conectar com o servidor');
        }
    });
    redefinirSenhaForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        const email = emailConfirmacao.value;
        const codigo = document.getElementById('codigo').value.trim();
        const novaSenha = document.getElementById('novaSenha').value;
        const confirmarSenha = document.getElementById('confirmarSenha').value;
        if (novaSenha !== confirmarSenha) {
            showError('As senhas não coincidem');
            return;
        }
        if (novaSenha.length < 8) {
            showError('A senha deve ter pelo menos 8 caracteres');
            return;
        }
        try {
            const response = await fetch('http://localhost:8080/api/usuario/confirmarAlteracaoSenha', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    mail: email,
                    codigo: codigo,
                    novaSenha: novaSenha,
                    confirmacaoSenha: confirmarSenha
                })
            });
            const responseData = await response.text();
            if (response.ok) {
                showSuccess('Senha alterada com sucesso! Redirecionando para login...', '/api/usuario/login.html', 3000);
            } else {
                let errorMsg = responseData;
                if (response.status === 401) {
                    errorMsg = 'Código inválido, expirado ou já utilizado';
                } else if (response.status === 400) {
                    errorMsg = responseData || 'Dados inválidos';
                }
                showError(errorMsg);
            }
        } catch (error) {
            console.error('Erro:', error);
            showError('Erro ao conectar com o servidor');
        }
    });
});