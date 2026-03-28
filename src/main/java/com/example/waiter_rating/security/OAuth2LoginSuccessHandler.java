package com.example.waiter_rating.security;

import com.example.waiter_rating.model.AppUser;
import com.example.waiter_rating.model.OAuthCodeToken;
import com.example.waiter_rating.model.UserRole;
import com.example.waiter_rating.repository.AppUserRepo;
import com.example.waiter_rating.repository.OAuthCodeTokenRepo;
import com.example.waiter_rating.service.ClientService;
import com.example.waiter_rating.service.EmailService;
import com.example.waiter_rating.service.NotificationService;
import com.example.waiter_rating.service.ProfessionalService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AppUserRepo appUserRepo;
    private final OAuthCodeTokenRepo oAuthCodeTokenRepo;
    private final ClientService clientService;
    private final ProfessionalService professionalService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public OAuth2LoginSuccessHandler(AppUserRepo appUserRepo,
                                     OAuthCodeTokenRepo oAuthCodeTokenRepo,
                                     ClientService clientService,
                                     ProfessionalService professionalService,
                                     EmailService emailService,
                                     NotificationService notificationService) {
        this.appUserRepo = appUserRepo;
        this.oAuthCodeTokenRepo = oAuthCodeTokenRepo;
        this.clientService = clientService;
        this.professionalService = professionalService;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");

        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        String userType = registrationId.equals("google-professional") ? "professional" : "client";

        log.info("Login con Google - Email: {}, Tipo: {}", email, userType);

        Optional<AppUser> existingUser = appUserRepo.findByEmail(email);

        // CASO 1: Quiere loguearse como PROFESSIONAL
        if ("professional".equalsIgnoreCase(userType)) {
            if (existingUser.isPresent()) {
                AppUser user = existingUser.get();

                // Si alguna vez fue profesional (activeRole PROFESSIONAL o tiene professionType)
                if (UserRole.PROFESSIONAL.equals(user.getActiveRole()) || user.getProfessionType() != null) {
                    log.info("Profesional existente autenticado: {}", user.getName());
                    user.setActiveRole(UserRole.PROFESSIONAL);
                    appUserRepo.save(user);
                    generateCodeAndRedirect(request, response, user, UserRole.PROFESSIONAL);
                    return;
                }

                // Si existe pero solo como client, error
                log.warn("Email ya registrado como Cliente: {}", email);
                String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/professional-login")
                        .queryParam("error", "email_already_registered_as_client")
                        .build()
                        .toUriString();
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }

            // No existe, crear nuevo professional
            log.info("Creando nuevo profesional: {}", email);
            AppUser newProfessional = professionalService.findOrCreateFromGoogle(email, name, googleId, emailVerified);
            emailService.sendWelcomeEmail(newProfessional.getEmail(), newProfessional.getName(), "PROFESSIONAL");
            notificationService.sendToUser(newProfessional.getId(), "¡Bienvenido/a a Calificalo!", "Hola " + newProfessional.getName().split(" ")[0] + ", tu cuenta de Profesional está lista. ¡Empezá a armar tu perfil!");
            generateCodeAndRedirect(request, response, newProfessional, UserRole.PROFESSIONAL);
            return;
        }

        // CASO 2: Quiere loguearse como CLIENT
        if ("client".equalsIgnoreCase(userType)) {
            if (existingUser.isPresent()) {
                AppUser user = existingUser.get();

                // Si tiene professional configurado, error
                if (user.getProfessionType() != null) {
                    log.warn("Email ya registrado como Profesional: {}", email);
                    String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/client-login")
                            .queryParam("error", "email_already_registered_as_professional")
                            .build()
                            .toUriString();
                    getRedirectStrategy().sendRedirect(request, response, errorUrl);
                    return;
                }

                // Si existe como client
                log.info("Cliente existente autenticado: {}", user.getName());
                user.setActiveRole(UserRole.CLIENT);
                appUserRepo.save(user);
                generateCodeAndRedirect(request, response, user, UserRole.CLIENT);
                return;
            }

            // No existe, crear nuevo client
            log.info("Creando nuevo cliente: {}", email);
            AppUser newClient = clientService.findOrCreateFromGoogle(email, name, googleId, emailVerified);
            emailService.sendWelcomeEmail(newClient.getEmail(), newClient.getName(), "CLIENT");
            notificationService.sendToUser(newClient.getId(), "¡Bienvenido/a a Calificalo!", "Hola " + newClient.getName().split(" ")[0] + ", tu cuenta de Cliente está lista. ¡Empezá a usar la plataforma!");
            generateCodeAndRedirect(request, response, newClient, UserRole.CLIENT);
            return;
        }
    }

    private void generateCodeAndRedirect(HttpServletRequest request,
                                         HttpServletResponse response,
                                         AppUser user,
                                         UserRole role) throws IOException {

        String code = UUID.randomUUID().toString().replace("-", "");
        OAuthCodeToken codeToken = new OAuthCodeToken(code, user, role);
        oAuthCodeTokenRepo.save(codeToken);

        log.info("Código OAuth generado para usuario: {}", user.getEmail());

        String redirectUrl;
        if (role == UserRole.PROFESSIONAL) {
            boolean hasCV = user.getCv() != null;
            String path = hasCV ? "/professional-dashboard" : "/edit-cv";
            redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + path)
                    .queryParam("code", code)
                    .build()
                    .toUriString();
        } else {
            redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/client-login")
                    .queryParam("code", code)
                    .build()
                    .toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}