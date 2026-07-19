package com.jobbed.company;

import com.jobbed.application.JobApplicationRepository;
import com.jobbed.common.error.exception.ResourceConflictException;
import com.jobbed.common.error.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock CompanyRepository companyRepository;
    @Mock JobApplicationRepository applicationRepository;

    CompanyService service;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new CompanyService(companyRepository, applicationRepository, new CompanyMapper());
    }

    private Company company(UUID id) {
        Company c = new Company();
        c.setId(id);
        c.setUserId(userId);
        c.setName("Acme");
        return c;
    }

    @Test
    void delete_withApplications_throwsConflict() {
        UUID id = UUID.randomUUID();
        when(companyRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(company(id)));
        when(applicationRepository.existsByCompany_IdAndUserId(id, userId)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(userId, id)).isInstanceOf(ResourceConflictException.class);
        verify(companyRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void delete_withoutApplications_deletes() {
        UUID id = UUID.randomUUID();
        Company company = company(id);
        when(companyRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(company));
        when(applicationRepository.existsByCompany_IdAndUserId(id, userId)).thenReturn(false);

        service.delete(userId, id);

        verify(companyRepository).delete(company);
    }

    @Test
    void get_notOwned_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(companyRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(userId, id)).isInstanceOf(ResourceNotFoundException.class);
    }
}
