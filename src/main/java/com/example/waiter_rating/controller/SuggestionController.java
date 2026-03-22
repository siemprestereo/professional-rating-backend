package com.example.waiter_rating.controller;

import com.example.waiter_rating.model.ContactMessage;
import com.example.waiter_rating.model.ContactMessageType;
import com.example.waiter_rating.repository.ContactMessageRepo;
import com.example.waiter_rating.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
@Slf4j
public class SuggestionController {

    private final EmailService emailService;
    private final ContactMessageRepo contactMessageRepo;

    @PostMapping("/profession")
    public ResponseEntity<?> suggestProfession(
            @RequestBody Map<String, String> body
    ) {
        String suggestion = body.get("suggestion");

        if (suggestion == null || suggestion.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La sugerencia no puede estar vacía"));
        }

        if (suggestion.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("error", "La sugerencia no puede superar los 100 caracteres"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String professionalEmail = authentication != null ? authentication.getName() : "desconocido";
        String professionalName = body.getOrDefault("professionalName", professionalEmail);

        emailService.sendProfessionSuggestionEmail(professionalName, professionalEmail, suggestion.trim());

        ContactMessage cm = new ContactMessage();
        cm.setType(ContactMessageType.SUGGESTION);
        cm.setSenderName(professionalName);
        cm.setSenderEmail(professionalEmail);
        cm.setMessage(suggestion.trim());
        contactMessageRepo.save(cm);

        return ResponseEntity.ok(Map.of("message", "Sugerencia enviada. ¡Gracias!"));
    }
}
