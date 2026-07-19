package com.jobbed.auth;

import com.jobbed.auth.token.RefreshTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Widerruft bei erkannter Token-Wiederverwendung alle Sitzungen eines Nutzers in
 * einer <em>eigenen</em> Transaktion ({@code REQUIRES_NEW}). Das ist notwendig,
 * weil der Aufrufer unmittelbar danach eine Exception wirft – der Widerruf muss
 * jedoch committet werden und darf nicht mit zurückgerollt werden.
 */
@Component
public class RefreshTokenReuseGuard {

    private final RefreshTokenRepository repository;

    public RefreshTokenReuseGuard(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllForUser(UUID userId) {
        repository.revokeAllForUser(userId);
    }
}
