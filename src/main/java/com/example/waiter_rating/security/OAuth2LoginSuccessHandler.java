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

        System.out.println("🔍 LOGIN CON GOOGLE:");
        System.out.println("   Email: " + email);
        System.out.println("   Name: " + name);
        System.out.println("   Google ID: " + googleId);

        // Verificar si es un Professional
        Professional professional = professionalService.findByEmail(email);

        if (professional != null) {
            // Es un Professional
            System.out.println("✅ Profesional autenticado: " + professional.getName());

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
            return;
        }

        // Si no es Professional, crear como Client
        Client client = clientService.findOrCreateFromGoogle(email, name, googleId, emailVerified);

        if (client.getId() != null) {
            System.out.println("✅ Cliente autenticado: " + client.getName() + " (ID: " + client.getId() + ")");

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
}