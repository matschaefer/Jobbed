package com.jobbed.company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/** Unternehmen, bei dem sich der Nutzer bewirbt. Nutzergebunden (Mandantentrennung). */
@Entity
@Table(name = "company", indexes = {
        @Index(name = "idx_company_user_name", columnList = "user_id, name")
})
@Getter
@Setter
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 255)
    private String website;

    @Column(length = 120)
    private String industry;

    @Column(name = "company_size", length = 50)
    private String companySize;

    @Column(length = 200)
    private String location;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
