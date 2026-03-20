package com.example.waiter_rating.service.impl;


import com.example.waiter_rating.model.AppUser;
import com.example.waiter_rating.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.mail.suggestions:hola@calificalo.com.ar}")
    private String suggestionsEmail;

    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("¡Bienvenido a Calificalo! 🎉");

            String htmlContent = buildWelcomeEmailTemplate(userName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendVerificationEmail(String toEmail, String userName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verifica tu cuenta en Calificalo ✅");

            String verificationUrl = frontendUrl + "/verify-email?token=" + token;
            String htmlContent = buildVerificationEmailTemplate(userName, verificationUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String userName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Recuperación de contraseña - Calificalo 🔐");

            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String htmlContent = buildPasswordResetEmailTemplate(userName, resetUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendProfessionSuggestionEmail(String professionalName, String professionalEmail, String suggestion) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(suggestionsEmail);
            helper.setSubject("Nueva sugerencia de profesión - Calificalo");

            String htmlContent = buildProfessionSuggestionTemplate(professionalName, professionalEmail, suggestion);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Profession suggestion email sent from: {}", professionalEmail);
        } catch (MessagingException e) {
            log.error("Failed to send profession suggestion email from: {}", professionalEmail, e);
        }
    }

    @Override
    @Async
    public void sendAdminEmail(String toEmail, String toName, String subject, String body, String replyTo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(replyTo, "Calificalo"));
            helper.setTo(toEmail);
            helper.setReplyTo(replyTo);
            helper.setSubject(subject);
            helper.setText(buildAdminEmailTemplate(toName, body, replyTo), true);

            mailSender.send(message);
            log.info("Admin email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send admin email to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendBroadcastEmail(List<AppUser> recipients, String subject, String body, String replyTo) {
        int sent = 0;
        for (AppUser user : recipients) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(new InternetAddress(replyTo, "Calificalo"));
                helper.setTo(user.getEmail());
                helper.setReplyTo(replyTo);
                helper.setSubject(subject);
                helper.setText(buildAdminEmailTemplate(user.getName(), body, replyTo), true);

                mailSender.send(message);
                sent++;
            } catch (Exception e) {
                log.error("Failed to send broadcast email to: {}", user.getEmail(), e);
            }
        }
        log.info("Broadcast email sent to {}/{} recipients", sent, recipients.size());
    }

    private String buildAdminEmailTemplate(String recipientName, String body, String replyTo) {
        String bodyHtml = body.replace("\n", "<br>");
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.7; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 28px 30px; border-radius: 10px 10px 0 0; }
                    .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; font-size: 15px; }
                    .footer { background: #f8f9fa; padding: 18px 20px; text-align: center; font-size: 12px; color: #888; border-radius: 0 0 10px 10px; border: 1px solid #e0e0e0; border-top: none; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2 style="margin:0; font-weight:300; font-size:22px;">Calificalo</h2>
                    </div>
                    <div class="content">
                        <p>Hola %s,</p>
                        <p>%s</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Calificalo. Todos los derechos reservados.</p>
                        <p>Podés responder este email a <a href="mailto:%s">%s</a></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(recipientName, bodyHtml, replyTo, replyTo);
    }

    private String buildProfessionSuggestionTemplate(String professionalName, String professionalEmail, String suggestion) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 24px 30px; border-radius: 10px 10px 0 0; }
                    .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
                    .suggestion-box { background: #f3f4f6; border-left: 4px solid #667eea; padding: 16px 20px; border-radius: 0 8px 8px 0; margin: 20px 0; font-size: 18px; font-weight: bold; color: #1f2937; }
                    .meta { font-size: 13px; color: #6b7280; margin-top: 20px; }
                    .footer { background: #f8f9fa; padding: 16px 20px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 10px 10px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2 style="margin:0">💡 Nueva sugerencia de profesión</h2>
                    </div>
                    <div class="content">
                        <p>Un profesional sugirió agregar una nueva categoría a Calificalo:</p>
                        <div class="suggestion-box">%s</div>
                        <div class="meta">
                            <strong>Enviado por:</strong> %s<br>
                            <strong>Email:</strong> %s
                        </div>
                    </div>
                    <div class="footer">
                        <p>© 2025 Calificalo. Panel interno.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(suggestion, professionalName, professionalEmail);
    }

    private String buildWelcomeEmailTemplate(String userName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Roboto', Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
                    .footer { background: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; padding: 12px 30px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>¡Bienvenido a Calificalo!</h1>
                    </div>
                    <div class="content">
                        <h2>Hola %s,</h2>
                        <p>Nos alegra tenerte con nosotros. Tu cuenta ha sido creada exitosamente.</p>
                        <p>Con Calificalo podrás:</p>
                        <ul>
                            <li>Calificar profesionales de manera rápida y segura</li>
                            <li>Ver el historial de tus calificaciones</li>
                            <li>Descubrir profesionales mejor valorados</li>
                        </ul>
                        <p>¡Comienza a explorar!</p>
                        <a href="%s" class="button">Ir a Calificalo</a>
                    </div>
                    <div class="footer">
                        <p>© 2025 Calificalo. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, frontendUrl);
    }

    private String buildVerificationEmailTemplate(String userName, String verificationUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Roboto', Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
                    .footer { background: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; padding: 12px 30px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Verifica tu cuenta</h1>
                    </div>
                    <div class="content">
                        <h2>Hola %s,</h2>
                        <p>Gracias por registrarte en Calificalo. Para completar tu registro, por favor verifica tu correo electrónico.</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Verificar mi cuenta</a>
                        </p>
                        <div class="warning">
                            <strong>⏰ Nota importante:</strong> Este enlace expirará en 24 horas.
                        </div>
                        <p>Si no te registraste en Calificalo, puedes ignorar este correo.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Calificalo. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, verificationUrl);
    }

    private String buildPasswordResetEmailTemplate(String userName, String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Roboto', Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
                    .footer { background: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; padding: 12px 30px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .warning { background: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Recuperación de contraseña</h1>
                    </div>
                    <div class="content">
                        <h2>Hola %s,</h2>
                        <p>Recibimos una solicitud para restablecer tu contraseña. Haz clic en el siguiente botón para crear una nueva contraseña:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Restablecer contraseña</a>
                        </p>
                        <div class="warning">
                            <strong>🔒 Seguridad:</strong> Este enlace expirará en 2 horas.<br>
                            Si no solicitaste este cambio, ignora este correo. Tu contraseña permanecerá segura.
                        </div>
                    </div>
                    <div class="footer">
                        <p>© 2025 Calificalo. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, resetUrl);
    }
}
