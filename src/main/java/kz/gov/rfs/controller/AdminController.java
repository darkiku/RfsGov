package kz.gov.rfs.controller;

import kz.gov.rfs.dto.ChangePasswordRequest;
import kz.gov.rfs.dto.DashboardStats;
import kz.gov.rfs.dto.UpdateRoleRequest;
import kz.gov.rfs.entity.AuditLog;
import kz.gov.rfs.entity.User;
import kz.gov.rfs.service.AdminService;
import kz.gov.rfs.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final AuditLogService auditLogService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody User user,
            @AuthenticationPrincipal User currentUser) {
        User created = adminService.createUser(user);
        auditLogService.log(currentUser, "CREATE", "User", created.getId(),
                "Created user: " + user.getUsername());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user,
            @AuthenticationPrincipal User currentUser) {
        User updated = adminService.updateUser(id, user);
        auditLogService.log(currentUser, "UPDATE", "User", id,
                "Updated user: " + user.getUsername());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal User currentUser) {
        User updated = adminService.updateUserRole(id, request.getRole());
        auditLogService.log(currentUser, "UPDATE", "User", id,
                "Changed role to: " + request.getRole());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<User> toggleUserStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        User updated = adminService.toggleUserStatus(id);
        auditLogService.log(currentUser, "UPDATE", "User", id,
                "Toggled user status to: " + updated.getIsActive());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        adminService.deleteUser(id);
        auditLogService.log(currentUser, "DELETE", "User", id, "Deleted user");
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/change-password")
    public ResponseEntity<User> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User currentUser) {
        User updated = adminService.changePassword(id, request.getNewPassword());
        auditLogService.log(currentUser, "UPDATE", "User", id, "Changed password");
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getAllLogs(pageable));
    }

    @GetMapping("/audit-logs/user/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogService.getLogsByUser(userId));
    }

    @GetMapping("/audit-logs/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(auditLogService.getLogsByEntity(entityType, entityId));
    }
}