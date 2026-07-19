package com.jobbed.application;

import com.jobbed.company.Company;
import com.jobbed.contact.ContactPerson;
import com.jobbed.tag.Tag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Zentrale Entität: eine Bewerbung eines Nutzers. Nutzergebunden
 * (Mandantentrennung strikt über {@code userId}).
 */
@Entity
@Table(name = "job_application", indexes = {
        @Index(name = "idx_application_user_status", columnList = "user_id, current_status"),
        @Index(name = "idx_application_user_company", columnList = "user_id, company_id"),
        @Index(name = "idx_application_user_date", columnList = "user_id, application_date"),
        @Index(name = "idx_application_user_next_action", columnList = "user_id, next_action_date")
})
@Getter
@Setter
@NoArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_person_id")
    private ContactPerson contactPerson;

    @Column(name = "job_title", nullable = false, length = 200)
    private String jobTitle;

    @Column(name = "job_description", columnDefinition = "text")
    private String jobDescription;

    @Column(length = 120)
    private String source;

    @Column(name = "job_url", length = 500)
    private String jobUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 30)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_model", length = 20)
    private WorkModel workModel;

    @Column(length = 200)
    private String location;

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Column(length = 3)
    private String currency;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 30)
    private ApplicationStatus currentStatus = ApplicationStatus.SAVED;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Priority priority = Priority.MEDIUM;

    private Short rating;

    private LocalDate deadline;

    @Column(name = "next_action_date")
    private LocalDate nextActionDate;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "rejection_reason", columnDefinition = "text")
    private String rejectionReason;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "application_tag",
            joinColumns = @JoinColumn(name = "application_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @BatchSize(size = 50)
    private Set<Tag> tags = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
