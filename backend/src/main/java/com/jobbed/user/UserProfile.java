package com.jobbed.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Erweiterte Profildaten eines Nutzers (1:1 zu {@link User}). Wird bei der
 * Wird bei der Registrierung leer angelegt und später durch den Nutzer befüllt.
 */
@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String phone;
    private String location;
    private String linkedInUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String preferredJobTitle;
    private String preferredLocations;

    @Column(precision = 12, scale = 2)
    private BigDecimal desiredSalary;

    @Column(length = 3)
    private String currency;

    private String noticePeriod;
    private String profileImageUrl;

    public UserProfile(User user) {
        this.user = user;
    }
}
