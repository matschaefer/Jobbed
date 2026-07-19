package com.jobbed.contact;

import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.company.Company;
import com.jobbed.company.CompanyService;
import com.jobbed.contact.dto.ContactRequest;
import com.jobbed.contact.dto.ContactResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Verwaltung von Ansprechpartnern (strikt nutzergebunden). */
@Service
public class ContactService {

    private final ContactPersonRepository contactRepository;
    private final CompanyService companyService;
    private final ContactMapper contactMapper;

    public ContactService(ContactPersonRepository contactRepository,
                          CompanyService companyService,
                          ContactMapper contactMapper) {
        this.contactRepository = contactRepository;
        this.companyService = companyService;
        this.contactMapper = contactMapper;
    }

    @Transactional(readOnly = true)
    public Page<ContactResponse> list(UUID userId, UUID companyId, Pageable pageable) {
        Page<ContactPerson> page = companyId != null
                ? contactRepository.findByUserIdAndCompanyId(userId, companyId, pageable)
                : contactRepository.findByUserId(userId, pageable);
        return page.map(contactMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> byCompany(UUID userId, UUID companyId) {
        return contactRepository.findByUserIdAndCompanyId(userId, companyId).stream()
                .map(contactMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ContactResponse get(UUID userId, UUID id) {
        return contactMapper.toResponse(requireOwned(userId, id));
    }

    @Transactional
    public ContactResponse create(UUID userId, ContactRequest request) {
        Company company = companyService.requireOwned(userId, request.companyId());
        ContactPerson contact = new ContactPerson();
        contact.setUserId(userId);
        contact.setCompany(company);
        apply(contact, request);
        return contactMapper.toResponse(contactRepository.save(contact));
    }

    @Transactional
    public ContactResponse update(UUID userId, UUID id, ContactRequest request) {
        ContactPerson contact = requireOwned(userId, id);
        if (!contact.getCompany().getId().equals(request.companyId())) {
            contact.setCompany(companyService.requireOwned(userId, request.companyId()));
        }
        apply(contact, request);
        return contactMapper.toResponse(contact);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        contactRepository.delete(requireOwned(userId, id));
    }

    /** Ownership-Prüfung für andere Services (z. B. Zuordnung zu einer Bewerbung). */
    @Transactional(readOnly = true)
    public ContactPerson requireOwned(UUID userId, UUID id) {
        return contactRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Ansprechpartner", id));
    }

    private void apply(ContactPerson contact, ContactRequest request) {
        contact.setFirstName(request.firstName().trim());
        contact.setLastName(request.lastName().trim());
        contact.setPosition(request.position());
        contact.setEmail(request.email());
        contact.setPhone(request.phone());
        contact.setLinkedInUrl(request.linkedInUrl());
        contact.setNotes(request.notes());
    }
}
