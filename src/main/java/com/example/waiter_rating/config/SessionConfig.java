package com.example.waiter_rating.config;

import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.ServletContext;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionConfig {

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return (ServletContext servletContext) -> {
            SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
            sessionCookieConfig.setHttpOnly(true);
            sessionCookieConfig.setSecure(true);  // Solo HTTPS
            sessionCookieConfig.setPath("/");
            sessionCookieConfig.setName("JSESSIONID");

            // CRÍTICO: Configurar SameSite=None
            sessionCookieConfig.setAttribute("SameSite", "None");
        };
    }
}