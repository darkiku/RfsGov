package kz.gov.rfs.controller;

import kz.gov.rfs.entity.Contact;
import kz.gov.rfs.entity.ContactType;
import kz.gov.rfs.entity.User;
import kz.gov.rfs.service.AuditLogService;
import kz.gov.rfs.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contactService;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Contact>> getContactsByType(@PathVariable ContactType type) {
        return ResponseEntity.ok(contactService.getContactsByType(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.getContactById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR','CONTACTS_MANAGER')")
    public ResponseEntity<Contact> createContact(
            @Valid @RequestBody Contact contact,
            @AuthenticationPrincipal User user) {
        Contact created = contactService.createContact(contact);
        auditLogService.log(user, "CREATE", "Contact", created.getId(),
                "Created contact: " + contact.getLabelRu());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR','CONTACTS_MANAGER')")
    public ResponseEntity<Contact> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody Contact contact,
            @AuthenticationPrincipal User user) {
        Contact updated = contactService.updateContact(id, contact);
        auditLogService.log(user, "UPDATE", "Contact", id,
                "Updated contact: " + contact.getLabelRu());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR','CONTACTS_MANAGER')")
    public ResponseEntity<Void> deleteContact(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        contactService.deleteContact(id);
        auditLogService.log(user, "DELETE", "Contact", id, "Deleted contact");
        return ResponseEntity.noContent().build();
    }
}