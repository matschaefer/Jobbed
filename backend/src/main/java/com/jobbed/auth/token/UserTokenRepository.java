package com.jobbed.auth.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {

    Optional<UserToken> findByTokenHashAndType(String tokenHash, UserTokenType type);

    @Modifying
    @Query("delete from UserToken t where t.userId = :userId and t.type = :type")
    int deleteByUserIdAndType(@Param("userId") UUID userId, @Param("type") UserTokenType type);
}
