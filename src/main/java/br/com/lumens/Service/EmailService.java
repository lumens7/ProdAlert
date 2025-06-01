package br.com.lumens.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import br.com.lumens.Exception.EmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/*
Criado por Luís
*/

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarEmailSimples(String destinatario, String assunto, String mensagem) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(destinatario);
            email.setSubject(assunto);
            email.setText(mensagem);
            mailSender.send(email);
        } catch (MailAuthenticationException e) {
            throw new EmailException("Falha na autenticação com o servidor de e-mail", e);
        } catch (MailSendException e) {
            throw new EmailException("Falha ao enviar o e-mail", e);
        } catch (Exception e) {
            throw new EmailException("Erro inesperado ao enviar e-mail", e);
        }
    }

    public void enviarEmailHtml(String destinatario, String assunto, String mensagemHtml) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(mensagemHtml, true);

            mailSender.send(mensagem);
        } catch (MessagingException e) {
            throw new EmailException("Falha ao enviar e-mail HTML", e);
        }
    }
}
