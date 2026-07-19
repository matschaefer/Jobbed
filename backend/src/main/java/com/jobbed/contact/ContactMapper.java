package com.jobbed.contact;

import com.jobbed.company.CompanyMapper;
import com.jobbed.contact.dto.ContactResponse;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {

    private final CompanyMapper companyMapper;

    public ContactMapper(CompanyMapper companyMapper) {
        this.companyMapper = companyMapper;
    }

    public ContactResponse toResponse(ContactPerson contact) {
        return new ContactResponse(
                contact.getId(),
                companyMapper.toSummary(contact.getCompany()),
                contact.getFirstName(),
                contact.getLastName(),
                contact.getPosition(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getLinkedInUrl(),
                contact.getNotes(),
                contact.getCreatedAt(),
                contact.getUpdatedAt());
    }
}
