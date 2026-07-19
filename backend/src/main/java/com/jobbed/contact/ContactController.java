package com.jobbed.contact;

import com.jobbed.common.web.ApiPage;
import com.jobbed.contact.dto.ContactRequest;
import com.jobbed.contact.dto.ContactResponse;
import com.jobbed.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contacts")
@Tag(name = "Contacts", description = "Ansprechpartner verwalten")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ApiPage<ContactResponse> list(@RequestParam(required = false) UUID companyId,
                                         @PageableDefault(size = 20) Pageable pageable) {
        return ApiPage.from(contactService.list(SecurityUtils.currentUserId(), companyId, pageable));
    }

    @GetMapping("/by-company/{companyId}")
    public List<ContactResponse> byCompany(@PathVariable UUID companyId) {
        return contactService.byCompany(SecurityUtils.currentUserId(), companyId);
    }

    @GetMapping("/{id}")
    public ContactResponse get(@PathVariable UUID id) {
        return contactService.get(SecurityUtils.currentUserId(), id);
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactRequest request) {
        ContactResponse created = contactService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ContactResponse update(@PathVariable UUID id, @Valid @RequestBody ContactRequest request) {
        return contactService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contactService.delete(SecurityUtils.currentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
