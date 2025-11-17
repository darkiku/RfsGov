package kz.gov.rfs.controller;

import kz.gov.rfs.entity.Procurement;
import kz.gov.rfs.entity.ProcurementType;
import kz.gov.rfs.entity.User;
import kz.gov.rfs.service.AuditLogService;
import kz.gov.rfs.service.ProcurementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/procurements")
@RequiredArgsConstructor
public class ProcurementController {
    private final ProcurementService procurementService;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Page<Procurement>> getAllProcurements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(procurementService.getAllActiveProcurements(pageable));
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<Procurement>> getProcurementsByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(procurementService.getProcurementsByYear(year));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Procurement>> getProcurementsByType(@PathVariable ProcurementType type) {
        return ResponseEntity.ok(procurementService.getProcurementsByType(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Procurement> getProcurementById(@PathVariable Long id) {
        return ResponseEntity.ok(procurementService.getProcurementById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER')")
    public ResponseEntity<Procurement> createProcurement(
            @Valid @RequestBody Procurement procurement,
            @AuthenticationPrincipal User user) {
        log.info("Creating procurement by user: {} with role: {}", user.getUsername(), user.getRole());
        Procurement created = procurementService.createProcurement(procurement);
        auditLogService.log(user, "CREATE", "Procurement", created.getId(),
                "Created procurement: " + procurement.getTitleRu());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER')")
    public ResponseEntity<Procurement> updateProcurement(
            @PathVariable Long id,
            @Valid @RequestBody Procurement procurement,
            @AuthenticationPrincipal User user) {
        log.info("Updating procurement {} by user: {}", id, user.getUsername());
        Procurement updated = procurementService.updateProcurement(id, procurement);
        auditLogService.log(user, "UPDATE", "Procurement", id,
                "Updated procurement: " + procurement.getTitleRu());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROCUREMENT_MANAGER')")
    public ResponseEntity<Void> deleteProcurement(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        log.info("Deleting procurement {} by user: {}", id, user.getUsername());
        procurementService.deleteProcurement(id);
        auditLogService.log(user, "DELETE", "Procurement", id, "Deleted procurement");
        return ResponseEntity.noContent().build();
    }
}