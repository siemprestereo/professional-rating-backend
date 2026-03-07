package com.example.waiter_rating.controller;

import com.example.waiter_rating.dto.request.PhotoConfirmRequest;
import com.example.waiter_rating.dto.response.AppUserResponse;
import com.example.waiter_rating.model.AppUser;
import com.example.waiter_rating.repository.AppUserRepo;
import com.example.waiter_rating.service.AppUserService;
import com.example.waiter_rating.service.CloudinaryService;
import com.example.waiter_rating.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class AppUserController {

    private final AppUserService userService;
    private final CloudinaryService cloudinaryService;
    private final AppUserRepo appUserRepo;
    private final JwtService jwtService;

    public AppUserController(AppUserService userService,
                             CloudinaryService cloudinaryService,
                             AppUserRepo appUserRepo,
                             JwtService jwtService) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
        this.appUserRepo = appUserRepo;
        this.jwtService = jwtService;
    }

    /** Obtener usuario por ID */
    @GetMapping("/{id}")
    public ResponseEntity<AppUserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    /** Listar todos los usuarios */
    @GetMapping
    public ResponseEntity<List<AppUserResponse>> listAll() {
        return ResponseEntity.ok(userService.listAll());
    }

    /** Verificar roles del usuario autenticado */
    @GetMapping("/me/roles")
    public ResponseEntity<Map<String, Object>> checkMyRoles(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(userService.checkUserRoles(authHeader));
    }

    /** Paso 1: solicitar parámetros firmados para subir foto a Cloudinary */
    @PostMapping("/photo/sign")
    public ResponseEntity<Map<String, Object>> getUploadSignature(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Claims claims = jwtService.validateToken(token);
            String email = claims.getSubject();

            AppUser user = appUserRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Map<String, Object> signedParams = cloudinaryService.generateSignedUploadParams(user.getId());
            return ResponseEntity.ok(signedParams);

        } catch (SecurityException e) {
            log.warn("Acceso no autorizado en photo/sign: {}", e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error generando firma de upload: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Paso 2: confirmar public_id luego de subir exitosamente a Cloudinary */
    @PutMapping("/photo")
    public ResponseEntity<Void> confirmPhoto(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid PhotoConfirmRequest request) {
        try {
            String token = authHeader.substring(7);
            Claims claims = jwtService.validateToken(token);
            String email = claims.getSubject();

            AppUser user = appUserRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            String photoUrl = cloudinaryService.verifyAndBuildUrl(user.getId(), request.getPublicId());
            userService.updateProfilePicture(user.getId(), photoUrl);

            log.info("Foto actualizada para user id: {}", user.getId());
            return ResponseEntity.ok().build();

        } catch (SecurityException e) {
            log.warn("Manipulación de public_id: {}", e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error confirmando foto: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}