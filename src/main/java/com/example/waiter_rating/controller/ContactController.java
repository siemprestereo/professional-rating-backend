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
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Slf4j
public class ContactController {

    private final ContactMessageRepo contactMessageRepo;
    private final EmailService emailService;

    @PostMapping("/support")
    public ResponseEntity<?> submitSupport(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El mensaje no puede estar vacío"));
        }
        if (message.length() > 1000) {
            return ResponseEntity.badRequest().body(Map.of("error", "El mensaje no puede superar los 1000 caracteres"));
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String senderEmail = auth != null ? auth.getName() : "desconocido";
        String senderName = body.getOrDefault("senderName", senderEmail);

        ContactMessage cm = new ContactMessage();
        cm.setType(ContactMessageType.SUPPORT);
        cm.setSenderName(senderName);
        cm.setSenderEmail(senderEmail);
        cm.setMessage(message.trim());
        contactMessageRepo.save(cm);

        log.info("Mensaje de soporte recibido de: {}", senderEmail);
        return ResponseEntity.ok(Map.of("message", "Mensaje enviado. ¡Gracias!"));
    }
}
