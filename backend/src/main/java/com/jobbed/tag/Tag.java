package com.jobbed.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Frei definierbares Schlagwort eines Nutzers; Bewerbungen können mehrere haben. */
@Entity
@Table(name = "tag", uniqueConstraints =
        @UniqueConstraint(name = "uq_tag_user_name", columnNames = {"user_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String color;
}
