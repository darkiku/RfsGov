package kz.gov.rfs.controller;

import kz.gov.rfs.entity.Department;
import kz.gov.rfs.entity.User;
import kz.gov.rfs.service.AuditLogService;
import kz.gov.rfs.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> createDepartment(
            @Valid @RequestBody Department department,
            @AuthenticationPrincipal User user) {
        Department created = departmentService.createDepartment(department);
        auditLogService.log(user, "CREATE", "Department", created.getId(),
                "Created department: " + department.getNameRu());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody Department department,
            @AuthenticationPrincipal User user) {
        Department updated = departmentService.updateDepartment(id, department);
        auditLogService.log(user, "UPDATE", "Department", id,
                "Updated department: " + department.getNameRu());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        departmentService.deleteDepartment(id);
        auditLogService.log(user, "DELETE", "Department", id, "Deleted department");
        return ResponseEntity.noContent().build();
    }
}