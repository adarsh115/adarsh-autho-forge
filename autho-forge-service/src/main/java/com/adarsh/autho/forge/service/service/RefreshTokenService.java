package com.adarsh.autho.forge.service.service;

import com.adarsh.autho.forge.service.dto.RefreshTokenDTO;
import com.adarsh.autho.forge.service.entity.RefreshToken;
import com.adarsh.autho.forge.service.repository.token.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(12);

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    /**
     * Generates a new refresh token for the user.
     * Saves hashed token in DB.
     * Returns raw token + entity (raw token transient).
     */
    public RefreshTokenDTO generateAndStore(Long userId) {

        // 1. Generate raw refresh token
        String rawToken = generateRawToken();

        // 2. Hash it before storing
        String hashed = bcrypt.encode(rawToken);

        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

        // 3. Build entity
        RefreshToken entity = RefreshToken.builder()
                .userId(userId)
                .refreshTokenHash(hashed)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        // 4. Save hashed token
        refreshTokenRepository.save(entity);


        return RefreshTokenDTO.builder()
                .rawToken(rawToken)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Validates refresh token for the user.
     * Does NOT auto-renew expired tokens (unsafe).
     * Deletes expired tokens.
     */
    public boolean validate(Long userId, String rawToken) {

        Optional<RefreshToken> storedOpt =
                refreshTokenRepository.findByUserIdAndRevokedFalse(userId);

        if (storedOpt.isEmpty()) {
            return false;
        }

        RefreshToken stored = storedOpt.get();

        // Check expiration
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            return false;
        }

        // Match hash
        return bcrypt.matches(rawToken, stored.getRefreshTokenHash());
    }

    /**
     * Called after successful refresh request.
     * Rotates (replaces) the old token with a new one.
     */
    public RefreshTokenDTO rotateToken(Long userId, String oldRawToken) {

        Optional<RefreshToken> storedOpt =
                refreshTokenRepository.findByUserIdAndRevokedFalse(userId);

        if (storedOpt.isEmpty()) {
            return null;
        }

        RefreshToken stored = storedOpt.get();

        // Reject invalid
        if (!bcrypt.matches(oldRawToken, stored.getRefreshTokenHash())) {
            return null;
        }

        // Mark old token as revoked
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        // Generate & return new one
        return generateAndStore(userId);
    }

    /**
     * Deletes all active tokens for a user (logout).
     */
    public void revokeAll(Long userId) {
        refreshTokenRepository.revokeAllTokens(userId);
    }

    // -------------- helpers -----------------

    private String generateRawToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256-bit secure random
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
