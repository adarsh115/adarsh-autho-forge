package com.adarsh.autho.forge.service.repository.token;

import com.adarsh.autho.forge.service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>, RefreshTokenCustom {
    void deleteByUserId(Long userId);

    Optional<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.userId = :userId")
    void revokeAllTokens(@Param("userId") Long userId);
}
