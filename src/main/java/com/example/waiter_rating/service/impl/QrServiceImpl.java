package com.example.waiter_rating.service.impl;

import com.example.waiter_rating.dto.response.QrCreateResponse;
import com.example.waiter_rating.model.AppUser;
import com.example.waiter_rating.model.QrToken;
import com.example.waiter_rating.model.UserRole;
import com.example.waiter_rating.model.WorkHistory;
import com.example.waiter_rating.repository.AppUserRepo;
import com.example.waiter_rating.repository.QrTokenRepo;
import com.example.waiter_rating.repository.WorkHistoryRepo;
import com.example.waiter_rating.service.QrService;
import com.example.waiter_rating.util.QrGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class QrServiceImpl implements QrService {

    private static final int DEFAULT_TTL_MIN = 3;
    private static final int MIN_TTL_MIN = 1;
    private static final int MAX_TTL_MIN = 5;

    private final QrTokenRepo qrRepo;
    private final AppUserRepo appUserRepo;
    private final WorkHistoryRepo workHistoryRepo;
    private final QrGenerator qrGenerator;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public QrServiceImpl(QrTokenRepo qrRepo,
                         AppUserRepo appUserRepo,
                         WorkHistoryRepo workHistoryRepo,
                         QrGenerator qrGenerator) {
        this.qrRepo = qrRepo;
        this.appUserRepo = appUserRepo;
        this.workHistoryRepo = workHistoryRepo;
        this.qrGenerator = qrGenerator;
    }

    @Override
    @Transactional
    public QrCreateResponse createDynamic(Long professionalId, Long businessId, int ttlMinutes) {
        AppUser professional = appUserRepo.findById(professionalId)
                .filter(user -> UserRole.PROFESSIONAL.equals(user.getActiveRole()))
                .orElseThrow(() -> new IllegalArgumentException("Professional no encontrado: " + professionalId));

        validateProfessionalHasActiveJob(professionalId);

        WorkHistory activeWork = workHistoryRepo.findByProfessionalIdAndIsActiveTrue(professionalId).stream()
                .filter(WorkHistory::getIsActive)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El profesional debe tener al menos un trabajo ACTUAL (activo) para generar un QR."));

        int ttl = ttlMinutes <= 0 ? DEFAULT_TTL_MIN
                : Math.max(MIN_TTL_MIN, Math.min(MAX_TTL_MIN, ttlMinutes));

        qrRepo.invalidateAllActiveForProfessional(professionalId);

        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        QrToken token = QrToken.builder()
                .code(code)
                .professional(professional)
                .business(activeWork.getBusiness())
                .expiresAt(LocalDateTime.now().plusMinutes(ttl))
                .active(true)
                .build();

        token = qrRepo.save(token);

        String qrUrl = frontendUrl + "/rate/" + token.getCode();

        byte[] pngBytes = qrGenerator.generatePng(qrUrl, 300, 300);
        String base64 = (pngBytes != null) ? Base64.getEncoder().encodeToString(pngBytes) : null;

        QrCreateResponse resp = new QrCreateResponse();
        resp.setCode(token.getCode());
        resp.setDeepLink(qrUrl);
        resp.setExpiresAt(token.getExpiresAt().toString() + "Z");
        resp.setQrPngBase64(base64);

        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public Long resolveProfessional(String code) {
        QrToken token = qrRepo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("QR inválido"));
        if (!token.isValidNow()) {
            throw new IllegalStateException("QR expirado o inactivo");
        }
        return token.getProfessional().getId();
    }

    @Override
    @Transactional
    public void invalidate(String code) {
        qrRepo.findByCode(code).ifPresent(t -> {
            t.setActive(false);
            qrRepo.save(t);
        });
    }

    private void validateProfessionalHasActiveJob(Long professionalId) {
        boolean hasActiveJob = workHistoryRepo.findByProfessionalIdAndIsActiveTrue(professionalId)
                .stream()
                .anyMatch(wh -> wh.getEndDate() == null);

        if (!hasActiveJob) {
            throw new IllegalStateException(
                    "El professional debe tener al menos un trabajo ACTUAL (activo) para generar un QR. " +
                            "Por favor, agregue un historial laboral actual."
            );
        }
    }
}