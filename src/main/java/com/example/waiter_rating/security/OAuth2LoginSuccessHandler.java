package com.example.waiter_rating.security;

import com.example.waiter_rating.model.AppUser;
import com.example.waiter_rating.model.UserRole;
import com.example.waiter_rating.repository.AppUserRepo;
import com.example.waiter_rating.service.ClientService;
import com.example.waiter_rating.service.JwtService;
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

@Component
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AppUserRepo appUserRepo;
    private final ClientService clientService;
    private final ProfessionalService professionalService;
    private final JwtService jwtService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public OAuth2LoginSuccessHandler(AppUserRepo appUserRepo,
                                     ClientService clientService,
                                     ProfessionalService professionalService,
                                     JwtService jwtService) {
        this.appUserRepo = appUserRepo;
        this.clientService = clientService;
        this.professionalService = professionalService;
        this.jwtService = jwtService;
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

        System.out.println("🔍 LOGIN CON GOOGLE:");
        System.out.println("   Email: " + email);
        System.out.println("   Name: " + name);
        System.out.println("   Google ID: " + googleId);
        System.out.println("   Registration ID: " + registrationId);
        System.out.println("   User Type: " + userType);

        Optional<AppUser> existingUser = appUserRepo.findByEmail(email);

        // CASO 1: Quiere loguearse como PROFESSIONAL
        if ("professional".equalsIgnoreCase(userType)) {
            if (existingUser.isPresent()) {
                AppUser user = existingUser.get();

                // Si ya existe y tiene professional configurado
                if (user.getProfessionType() != null) {
                    System.out.println("✅ Profesional existente autenticado: " + user.getName());
                    user.setActiveRole(UserRole.PROFESSIONAL);
                    appUserRepo.save(user);
                    authenticateAndRedirect(request, response, user, UserRole.PROFESSIONAL);
                    return;
                }

                // Si existe pero solo como client, error
                System.out.println("❌ Error: Email ya registrado como Cliente");
                String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/professional-login")
                        .queryParam("error", "email_already_registered_as_client")
                        .build()
                        .toUriString();
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }

            // No existe, crear nuevo professional
            System.out.println("➕ Creando nuevo profesional");
            AppUser newProfessional = professionalService.findOrCreateFromGoogle(email, name, googleId, emailVerified);
            authenticateAndRedirect(request, response, newProfessional, UserRole.PROFESSIONAL);
            return;
        }

        // CASO 2: Quiere loguearse como CLIENT
        if ("client".equalsIgnoreCase(userType)) {
            if (existingUser.isPresent()) {
                AppUser user = existingUser.get();

                // Si tiene professional configurado, error
                if (user.getProfessionType() != null) {
                    System.out.println("❌ Error: Email ya registrado como Profesional");
                    String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/client-login")
                            .queryParam("error", "email_already_registered_as_professional")
                            .build()
                            .toUriString();
                    getRedirectStrategy().sendRedirect(request, response, errorUrl);
                    return;
                }

                // Si existe como client
                System.out.println("✅ Cliente existente autenticado: " + user.getName());
                user.setActiveRole(UserRole.CLIENT);
                appUserRepo.save(user);
                authenticateAndRedirect(request, response, user, UserRole.CLIENT);
                return;
            }

            // No existe, crear nuevo client
            System.out.println("➕ Creando nuevo cliente");
            AppUser newClient = clientService.findOrCreateFromGoogle(email, name, googleId, emailVerified);
            authenticateAndRedirect(request, response, newClient, UserRole.CLIENT);
            return;
        }
    }

    private void authenticateAndRedirect(HttpServletRequest request,
                                         HttpServletResponse response,
                                         AppUser user,
                                         UserRole role) throws IOException {

        // Generamos el token (que ahora durará 15 días según tu properties)
        String token = jwtService.generateToken(
                user.getId(),
                role.name(),
                user.getEmail(),
                user.getName()
        );

        // Cambiamos System.out por loggers (más profesional y seguro)
        log.info("Sesión iniciada exitosamente para el usuario con email: {}", user.getEmail());

        String redirectUrl;
        if (role == UserRole.PROFESSIONAL) {
            boolean hasCompleteProfile = user.getCv() != null;
            String path = hasCompleteProfile ? "/professional-dashboard" : "/professional-register";

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(frontendUrl + path)
                    .queryParam("token", token);

            if (!hasCompleteProfile) {
                builder.queryParam("step", "complete-profile");
            }
            redirectUrl = builder.build().toUriString();
        } else {
            redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/client-dashboard")
                    .queryParam("token", token)
                    .build()
                    .toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}