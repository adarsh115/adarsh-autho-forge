package com.adarsh.autho.forge.service;

import com.adarsh.autho.forge.config.AuthoForgeProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service to fetch and cache JWK (JSON Web Key) from the auth server.
 * Implements thread-safe caching with automatic refresh.
 */
@Service
public class JwkService {

    private static final Logger log = LoggerFactory.getLogger(JwkService.class);

    private final AuthoForgeProperties properties;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private JWKSet cachedJwkSet;
    private Instant cacheExpiry;

    public JwkService(AuthoForgeProperties properties) {
        this.properties = properties;
    }

    /**
     * Get RSA public key by Key ID (kid).
     * Fetches from cache or refreshes if expired.
     */
    public RSAKey getPublicKey(String keyId) {
        JWKSet jwkSet = getJwkSet();
        
        try {
            return (RSAKey) jwkSet.getKeyByKeyId(keyId);
        } catch (Exception e) {
            log.error("Failed to get public key with kid={}", keyId, e);
            throw new RuntimeException("Public key not found for kid: " + keyId, e);
        }
    }

    /**
     * Get the JWK set, using cache if valid, otherwise fetch fresh.
     */
    private JWKSet getJwkSet() {
        // Try read lock first (fast path)
        lock.readLock().lock();
        try {
            if (cachedJwkSet != null && Instant.now().isBefore(cacheExpiry)) {
                return cachedJwkSet;
            }
        } finally {
            lock.readLock().unlock();
        }

        // Cache expired or missing, acquire write lock to refresh
        lock.writeLock().lock();
        try {
            // Double-check in case another thread already refreshed
            if (cachedJwkSet != null && Instant.now().isBefore(cacheExpiry)) {
                return cachedJwkSet;
            }

            // Fetch fresh JWK set
            log.info("Fetching JWK set from: {}", properties.getJwkSetUri());
            cachedJwkSet = JWKSet.load(new URL(properties.getJwkSetUri()));
            cacheExpiry = Instant.now().plusSeconds(properties.getJwkCacheDurationMinutes() * 60);
            log.info("JWK set cached successfully, expires at: {}", cacheExpiry);

            return cachedJwkSet;
        } catch (Exception e) {
            log.error("Failed to fetch JWK set from {}", properties.getJwkSetUri(), e);
            throw new RuntimeException("Failed to fetch JWK set", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Force refresh of the JWK cache (useful for key rotation).
     */
    public void refreshCache() {
        lock.writeLock().lock();
        try {
            cacheExpiry = Instant.now().minusSeconds(1); // Force expiry
            log.info("JWK cache invalidated, will refresh on next request");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
