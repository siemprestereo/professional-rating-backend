package com.example.waiter_rating.security;

import com.example.waiter_rating.model.Client;
import com.example.waiter_rating.model.Professional;
import com.example.waiter_rating.service.ClientService;
import com.example.waiter_rating.service.JwtService;
import com.example.waiter_rating.service.ProfessionalService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final ClientService clientService;
    private final ProfessionalService professionalService;
    private final JwtService jwtService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public OAuth2LoginSuccessHandler(ClientService clientService,
                                     ProfessionalService professionalService,
                                     JwtService jwtService) {
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

        // Obtener el ID de registro (google-client o google-professional)
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        String userType = registrationId.equals("google-professional") ? "professional" : "client";

        System.out.println("🔍 LOGIN CON GOOGLE:");
        System.out.println("   Email: " + email);
        System.out.println("   Name: " + name);
        System.out.println("   Google ID: " + googleId);
        System.out.println("   Registration ID: " + registrationId);
        System.out.println("   User Type: " + userType);

        // Buscar si existe como Professional
        Professional professional = professionalService.findByEmail(email);

        // Buscar si existe como Client
        Client client = clientService.findByEmail(email);

        // CASO 1: Quiere loguearse como PROFESSIONAL
        if ("professional".equalsIgnoreCase(userType)) {
            // Verificar que NO existe como cliente
            if (client != null) {
                System.out.println("❌ Error: Email ya registrado como Cliente");
                String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/professional-login")
                        .queryParam("error", "email_already_registered_as_client")
                        .build()
                        .toUriString();
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }

            // Si existe como professional, autenticar
            if (professional != null) {
                System.out.println("✅ Profesional existente autenticado: " + professional.getName());
                authenticateAndRedirectProfessional(request, response, professional);
                return;
            }

            // Si no existe, crear nuevo professional
            System.out.println("➕ Creando nuevo profesional");
            Professional newProfessional = professionalService.findOrCreateFromGoogle(email, name, googleId, emailVerified);
            authenticateAndRedirectProfessional(request, response, newProfessional);
            return;
        }

        // CASO 2: Quiere loguearse como CLIENT
        if ("client".equalsIgnoreCase(userType)) {
            // Verificar que NO existe como profesional
            if (professional != null) {
                System.out.println("❌ Error: Email ya registrado como Profesional");
                String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/client-login")
                        .queryParam("error", "email_already_registered_as_professional")
                        .build()
                        .toUriString();
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }

            // Si existe como client, autenticar
            if (client != null) {
                System.out.println("✅ Cliente existente autenticado: " + client.getName());
                authenticateAndRedirectClient(request, response, client);
                return;
            }

            // Si no existe, crear nuevo client
            System.out.println("➕ Creando nuevo cliente");
            Client newClient = clientService.findOrCreateFromGoogle(email, name, googleId, emailVerified);
            authenticateAndRedirectClient(request, response, newClient);
            return;
        }
    }

    private void authenticateAndRedirectProfessional(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     Professional professional) throws IOException {
        // Generar JWT
        String token = jwtService.generateToken(
                professional.getId(),
                "PROFESSIONAL",
                professional.getEmail(),
                professional.getName()
        );

        System.out.println("🔑 JWT generado para profesional: " + professional.getEmail());

        // Verificar si tiene perfil completo
        boolean hasCompleteProfile = professional.getCv() != null;

        String redirectUrl;
        if (hasCompleteProfile) {
            redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/professional-dashboard")
                    .queryParam("token", token)
                    .build()
                    .toUriString();
        } else {
            redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/professional-register")
                    .queryParam("step", "complete-profile")
                    .queryParam("token", token)
                    .build()
                    .toUriString();
        }

        System.out.println("🔄 Redirigiendo profesional a: " + redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void authenticateAndRedirectClient(HttpServletRequest request,
                                               HttpServletResponse response,
                                               Client client) throws IOException {
        // Generar JWT
        String token = jwtService.generateToken(
                client.getId(),
                "CLIENT",
                client.getEmail(),
                client.getName()
        );

        System.out.println("🔑 JWT generado para cliente: " + client.getEmail());

        // Redirigir cliente a su dashboard con el token en la URL
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/client-dashboard")
                .queryParam("token", token)
                .build()
                .toUriString();

        System.out.println("🔄 Redirigiendo cliente a: " + redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}