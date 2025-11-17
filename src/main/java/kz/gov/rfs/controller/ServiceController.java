package kz.gov.rfs.controller;

import kz.gov.rfs.entity.Service;
import kz.gov.rfs.entity.ServiceType;
import kz.gov.rfs.entity.User;
import kz.gov.rfs.service.AuditLogService;
import kz.gov.rfs.service.ServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceService serviceService;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<Service>> getAllServices() {
        return ResponseEntity.ok(serviceService.getAllActiveServices());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Service>> getServicesByType(@PathVariable ServiceType type) {
        return ResponseEntity.ok(serviceService.getServicesByType(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Service> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.getServiceById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SERVICES_MANAGER')")
    public ResponseEntity<Service> createService(
            @Valid @RequestBody Service service,
            @AuthenticationPrincipal User user) {
        log.info("Creating service by user: {} with role: {}", user.getUsername(), user.getRole());
        Service created = serviceService.createService(service);
        auditLogService.log(user, "CREATE", "Service", created.getId(),
                "Created service: " + service.getTitleRu());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICES_MANAGER')")
    public ResponseEntity<Service> updateService(
            @PathVariable Long id,
            @Valid @RequestBody Service service,
            @AuthenticationPrincipal User user) {
        log.info("Updating service {} by user: {}", id, user.getUsername());
        Service updated = serviceService.updateService(id, service);
        auditLogService.log(user, "UPDATE", "Service", id,
                "Updated service: " + service.getTitleRu());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICES_MANAGER')")
    public ResponseEntity<Void> deleteService(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        log.info("Deleting service {} by user: {}", id, user.getUsername());
        serviceService.deleteService(id);
        auditLogService.log(user, "DELETE", "Service", id, "Deleted service");
        return ResponseEntity.noContent().build();
    }
}