package com.adarsh.autho.forge.service.repository.token;

import com.adarsh.autho.forge.service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>, RefreshTokenCustom {
    void deleteByUserId(String userId);

    Optional<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

    void revokeAllTokens(Long userId);
}
