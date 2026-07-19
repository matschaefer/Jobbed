package com.jobbed.document;

import com.jobbed.application.*;
import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.company.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @Mock DocumentRepository documents; @Mock JobApplicationRepository applications;
    @Mock ApplicationActivityRepository activities; @Mock FileStorageService storage;
    DocumentService service;
    @BeforeEach void setUp() { service = new DocumentService(documents, applications, activities, storage, new FileContentValidator(), 10); }

    @Test void uploadChecksOwnershipStoresDetectedTypeAndRecordsActivity() {
        UUID userId = UUID.randomUUID(), appId = UUID.randomUUID();
        Company company = new Company(); company.setId(UUID.randomUUID());
        JobApplication app = new JobApplication(); app.setId(appId); app.setUserId(userId); app.setCompany(company);
        when(applications.findByIdAndUserId(appId, userId)).thenReturn(Optional.of(app));
        when(storage.store(any(), eq("pdf"))).thenReturn(new StoredFile("uuid.pdf", "2026/07/uuid.pdf"));
        when(documents.save(any())).thenAnswer(inv -> { Document d = inv.getArgument(0); d.setId(UUID.randomUUID()); return d; });
        var file = new MockMultipartFile("file", "../cv.pdf", "application/pdf", "%PDF-1.7\nCV".getBytes());

        var result = service.upload(userId, appId, DocumentType.CV, null, file);

        assertThat(result.originalFileName()).isEqualTo("cv.pdf");
        assertThat(result.mimeType()).isEqualTo("application/pdf");
        verify(activities).save(argThat(a -> a.getActivityType() == ActivityType.DOCUMENT_UPLOADED));
    }
    @Test void foreignApplicationIsHiddenAsNotFound() {
        UUID userId = UUID.randomUUID(), appId = UUID.randomUUID();
        when(applications.findByIdAndUserId(appId, userId)).thenReturn(Optional.empty());
        var file = new MockMultipartFile("file", "cv.pdf", "application/pdf", "%PDF-1.7".getBytes());
        assertThatThrownBy(() -> service.upload(userId, appId, DocumentType.CV, null, file))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(storage);
    }

    @Test void foreignDocumentDownloadIsHiddenAndNeverTouchesStorage() {
        UUID userId = UUID.randomUUID(), documentId = UUID.randomUUID();
        when(documents.findByIdAndUserId(documentId, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.download(userId, documentId)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(storage);
    }
}
