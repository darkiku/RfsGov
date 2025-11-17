package kz.gov.rfs.controller;

import kz.gov.rfs.entity.About;
import kz.gov.rfs.entity.AboutSection;
import kz.gov.rfs.entity.User;
import kz.gov.rfs.service.AboutService;
import kz.gov.rfs.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/about")
@RequiredArgsConstructor
public class AboutController {
    private final AboutService aboutService;
    private final AuditLogService auditLogService;

    @GetMapping("/section/{section}")
    public ResponseEntity<List<About>> getAboutBySection(@PathVariable AboutSection section) {
        return ResponseEntity.ok(aboutService.getAboutBySection(section));
    }

    @GetMapping("/key/{sectionKey}")
    public ResponseEntity<About> getAboutBySectionKey(@PathVariable String sectionKey) {
        return ResponseEntity.ok(aboutService.getAboutBySectionKey(sectionKey));
    }

    @GetMapping("/{id}")
    public ResponseEntity<About> getAboutById(@PathVariable Long id) {
        return ResponseEntity.ok(aboutService.getAboutById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ABOUT_MANAGER')")
    public ResponseEntity<About> createAbout(
            @Valid @RequestBody About about,
            @AuthenticationPrincipal User user) {
        About created = aboutService.createAbout(about);
        auditLogService.log(user, "CREATE", "About", created.getId(),
                "Created about section: " + about.getSectionKey());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ABOUT_MANAGER')")
    public ResponseEntity<About> updateAbout(
            @PathVariable Long id,
            @Valid @RequestBody About about,
            @AuthenticationPrincipal User user) {
        About updated = aboutService.updateAbout(id, about);
        auditLogService.log(user, "UPDATE", "About", id,
                "Updated about section: " + about.getSectionKey());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ABOUT_MANAGER')")
    public ResponseEntity<Void> deleteAbout(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        aboutService.deleteAbout(id);
        auditLogService.log(user, "DELETE", "About", id, "Deleted about section");
        return ResponseEntity.noContent().build();
    }
}