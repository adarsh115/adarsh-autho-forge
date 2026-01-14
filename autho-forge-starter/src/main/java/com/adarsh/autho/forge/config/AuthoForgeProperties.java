package com.adarsh.autho.forge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Autho-Forge authentication.
 * 
 * Usage in application.properties:
 * autho.forge.jwk-set-uri=http://localhost:8080/.well-known/jwks.json
 * autho.forge.issuer=https://adarsh-autho-forge
 * autho.forge.enabled=true
 */
@ConfigurationProperties(prefix = "autho.forge")
public class AuthoForgeProperties {

    /**
     * URI to fetch JWK (JSON Web Key) set from the auth server
     */
    private String jwkSetUri;

    /**
     * Expected issuer claim in JWT tokens
     */
    private String issuer;

    /**
     * Enable/disable authentication (useful for testing)
     */
    private boolean enabled = true;

    /**
     * Cache duration for JWK keys in minutes
     */
    private long jwkCacheDurationMinutes = 60;

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getJwkCacheDurationMinutes() {
        return jwkCacheDurationMinutes;
    }

    public void setJwkCacheDurationMinutes(long jwkCacheDurationMinutes) {
        this.jwkCacheDurationMinutes = jwkCacheDurationMinutes;
    }
}
