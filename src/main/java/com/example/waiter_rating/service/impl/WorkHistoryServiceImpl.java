package com.example.waiter_rating.service.impl;

import com.example.waiter_rating.dto.request.WorkHistoryRequest;
import com.example.waiter_rating.model.Business;
import com.example.waiter_rating.model.BusinessType;
import com.example.waiter_rating.model.Professional;
import com.example.waiter_rating.model.WorkHistory;
import com.example.waiter_rating.repository.BusinessRepo;
import com.example.waiter_rating.repository.ProfessionalRepo;
import com.example.waiter_rating.repository.WorkHistoryRepo;
import com.example.waiter_rating.service.WorkHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class WorkHistoryServiceImpl implements WorkHistoryService {

    private final WorkHistoryRepo workHistoryRepo;
    private final ProfessionalRepo professionalRepo;
    private final BusinessRepo businessRepo;

    public WorkHistoryServiceImpl(WorkHistoryRepo workHistoryRepo,
                                  ProfessionalRepo professionalRepo,
                                  BusinessRepo businessRepo) {
        this.workHistoryRepo = workHistoryRepo;
        this.professionalRepo = professionalRepo;
        this.businessRepo = businessRepo;
    }

    @Override
    @Transactional
    public WorkHistory addWorkHistory(Long professionalId, WorkHistoryRequest request) {
        Professional professional = professionalRepo.findById(professionalId)
                .orElseThrow(() -> new IllegalArgumentException("Professional no encontrado: " + professionalId));

        // Determinar si es trabajo activo
        boolean isActiveJob = (request.getEndDate() == null);

        // Validar límites SOLO si es trabajo ACTIVO
        if (isActiveJob) {
            // Validar máximo 3 trabajos activos simultáneos
            validateMaxActiveJobs(professionalId);

            // Validar límite mensual de cambios
            validateMonthlyWorkplaceChanges(professional);
        }

        // Crear Business (siempre nuevo, sin búsqueda ni reutilización)
        Business business;
        if (request.getBusinessId() != null) {
            // Si viene businessId, usarlo (caso poco común)
            business = businessRepo.findById(request.getBusinessId())
                    .orElseThrow(() -> new IllegalArgumentException("Business no encontrado: " + request.getBusinessId()));
        } else {
            // Siempre crear un nuevo Business con el nombre ingresado
            business = Business.builder()
                    .name(request.getBusinessName())
                    .businessType(BusinessType.RESTAURANT) // Default
                    .build();
            business = businessRepo.save(business);
        }

        WorkHistory workHistory = WorkHistory.builder()
                .professional(professional)
                .business(business)
                .businessName(request.getBusinessName())
                .position(request.getPosition())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(isActiveJob)
                .isFreelance(request.getIsFreelance() != null ? request.getIsFreelance() : false) // ← AGREGAR ESTA LÍNEA
                .referenceContact(request.getReferenceContact())
                .description(request.getDescription())
                .build();

        workHistory = workHistoryRepo.save(workHistory);

        // Registrar cambio de workplace SOLO si es trabajo activo
        if (isActiveJob) {
            professional.registerWorkplaceChange();
            professionalRepo.save(professional);
        }

        return workHistory;
    }

    @Override
    @Transactional
    public WorkHistory updateWorkHistory(Long professionalId, Long workHistoryId, WorkHistoryRequest request) {
        Professional professional = professionalRepo.findById(professionalId)
                .orElseThrow(() -> new IllegalArgumentException("Professional no encontrado: " + professionalId));

        WorkHistory workHistory = workHistoryRepo.findById(workHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("WorkHistory no encontrado: " + workHistoryId));

        // Verificar que el WorkHistory pertenece al professional
        if (!workHistory.getProfessional().getId().equals(professionalId)) {
            throw new IllegalArgumentException("Este trabajo no pertenece al professional especificado");
        }

        boolean wasActive = workHistory.getIsActive();
        boolean willBeActive = (request.getEndDate() == null);

        // Si está cambiando de inactivo a activo, validar límites
        if (!wasActive && willBeActive) {
            validateMaxActiveJobs(professionalId);
            validateMonthlyWorkplaceChanges(professional);

            professional.registerWorkplaceChange();
            professionalRepo.save(professional);
        }

        // Actualizar business (siempre crear nuevo, sin búsqueda)
        Business business;
        if (request.getBusinessId() != null) {
            business = businessRepo.findById(request.getBusinessId())
                    .orElseThrow(() -> new IllegalArgumentException("Business no encontrado: " + request.getBusinessId()));
        } else {
            // Siempre crear un nuevo Business con el nombre ingresado
            business = Business.builder()
                    .name(request.getBusinessName())
                    .businessType(BusinessType.RESTAURANT)
                    .build();
            business = businessRepo.save(business);
        }

        workHistory.setBusiness(business);
        workHistory.setBusinessName(request.getBusinessName());
        workHistory.setPosition(request.getPosition());
        workHistory.setStartDate(request.getStartDate());
        workHistory.setEndDate(request.getEndDate());
        workHistory.setIsActive(willBeActive);
        workHistory.setIsFreelance(request.getIsFreelance() != null ? request.getIsFreelance() : false); // ← AGREGAR ESTA LÍNEA
        workHistory.setReferenceContact(request.getReferenceContact());
        workHistory.setDescription(request.getDescription());

        return workHistoryRepo.save(workHistory);
    }

    @Override
    @Transactional
    public WorkHistory closeWorkHistory(Long professionalId, Long workHistoryId, LocalDate endDate) {
        WorkHistory workHistory = workHistoryRepo.findById(workHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("WorkHistory no encontrado: " + workHistoryId));

        // Verificar que el WorkHistory pertenece al professional
        if (!workHistory.getProfessional().getId().equals(professionalId)) {
            throw new IllegalArgumentException("Este trabajo no pertenece al professional especificado");
        }

        workHistory.closeJob(endDate);
        return workHistoryRepo.save(workHistory);
    }

    @Override
    @Transactional
    public void deleteWorkHistory(Long professionalId, Long workHistoryId) {
        WorkHistory workHistory = workHistoryRepo.findById(workHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("WorkHistory no encontrado: " + workHistoryId));

        // Verificar que el WorkHistory pertenece al professional
        if (!workHistory.getProfessional().getId().equals(professionalId)) {
            throw new IllegalArgumentException("Este trabajo no pertenece al professional especificado");
        }

        workHistoryRepo.delete(workHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkHistory> listWorkHistory(Long professionalId) {
        return workHistoryRepo.findByProfessionalId(professionalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkHistory> listActiveWorkHistory(Long professionalId) {
        return workHistoryRepo.findByProfessionalIdAndIsActiveTrue(professionalId);
    }

    @Override
    @Transactional
    public WorkHistory enableFreelanceWork(Long professionalId) {
        Professional professional = professionalRepo.findById(professionalId)
                .orElseThrow(() -> new IllegalArgumentException("Professional no encontrado: " + professionalId));

        // Verificar si ya tiene freelance activo
        Optional<WorkHistory> existingFreelance = workHistoryRepo.findByProfessionalIdAndIsActiveTrue(professionalId)
                .stream()
                .filter(wh -> wh.getBusiness() != null && wh.getBusiness().getBusinessType() == BusinessType.FREELANCE)
                .findFirst();

        if (existingFreelance.isPresent()) {
            throw new IllegalStateException("El professional ya tiene trabajo independiente activo");
        }

        // Crear o buscar Business de tipo FREELANCE para este profesional
        String freelanceName = professional.getName() + " - Independiente";
        Business freelanceBusiness = businessRepo.findByNameIgnoreCase(freelanceName)
                .orElseGet(() -> {
                    Business newBusiness = Business.builder()
                            .name(freelanceName)
                            .businessType(BusinessType.FREELANCE)
                            .build();
                    return businessRepo.save(newBusiness);
                });

        // Crear WorkHistory
        WorkHistory workHistory = WorkHistory.builder()
                .professional(professional)
                .business(freelanceBusiness)
                .businessName("Independiente")
                .position("Trabajo Autónomo")
                .startDate(LocalDate.now())
                .isActive(true)
                .isFreelance(true)
                .build();

        return workHistoryRepo.save(workHistory);
    }

    @Override
    @Transactional
    public void disableFreelanceWork(Long professionalId) {
        Professional professional = professionalRepo.findById(professionalId)
                .orElseThrow(() -> new IllegalArgumentException("Professional no encontrado: " + professionalId));

        // Buscar el WorkHistory freelance activo
        Optional<WorkHistory> freelanceWork = workHistoryRepo.findByProfessionalIdAndIsActiveTrue(professionalId)
                .stream()
                .filter(wh -> wh.getBusiness() != null && wh.getBusiness().getBusinessType() == BusinessType.FREELANCE)
                .findFirst();

        if (freelanceWork.isEmpty()) {
            throw new IllegalStateException("No se encontró trabajo independiente activo para desactivar");
        }

        // Cerrar el trabajo freelance
        WorkHistory work = freelanceWork.get();
        work.closeJob(LocalDate.now());
        workHistoryRepo.save(work);
    }

    /**
     * Valida que el professional no tenga más de 3 trabajos activos simultáneos
     */
    private void validateMaxActiveJobs(Long professionalId) {
        List<WorkHistory> activeJobs = workHistoryRepo.findByProfessionalIdAndIsActiveTrue(professionalId);

        if (activeJobs.size() >= 3) {
            throw new IllegalStateException(
                    "Ya tenés 3 trabajos activos. Marcá uno como finalizado antes de agregar otro trabajo actual."
            );
        }
    }

    /**
     * Valida que el professional no haya excedido el límite de cambios mensuales
     * Este límite solo aplica a trabajos ACTIVOS
     */
    private void validateMonthlyWorkplaceChanges(Professional professional) {
        if (!professional.canChangeWorkplace()) {
            throw new IllegalStateException(
                    "Has alcanzado el límite de 3 cambios de trabajo activo por mes. " +
                            "Podrás agregar más a partir del próximo mes. " +
                            "Nota: Podés seguir agregando trabajos anteriores (finalizados) sin problema."
            );
        }
    }
}