package com.adarsh.autho.forge.filter;

import com.adarsh.autho.forge.config.AuthoForgeProperties;
import com.adarsh.autho.forge.security.JwtAuthenticationToken;
import com.adarsh.autho.forge.service.JwkService;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Filter to validate JWT tokens from Authorization header.
 * Extracts user information and sets Spring Security context.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwkService jwkService;
    private final AuthoForgeProperties properties;

    public JwtAuthenticationFilter(JwkService jwkService, AuthoForgeProperties properties) {
        this.jwkService = jwkService;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractToken(request);
            
            if (token != null) {
                JwtAuthenticationToken authentication = validateAndCreateAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication successful for user: {}", authentication.getUsername());
            }
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            response.setContentType("application/json");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Validate JWT and create authentication token
     */
    private JwtAuthenticationToken validateAndCreateAuthentication(String token) throws Exception {
        // Parse JWT
        SignedJWT signedJWT = SignedJWT.parse(token);
        
        // Get Key ID from header
        String keyId = signedJWT.getHeader().getKeyID();
        if (keyId == null) {
            throw new SecurityException("JWT missing 'kid' in header");
        }

        // Fetch public key
        RSAKey publicKey = jwkService.getPublicKey(keyId);
        if (publicKey == null) {
            throw new SecurityException("Public key not found for kid: " + keyId);
        }

        // Verify signature
        JWSVerifier verifier = new RSASSAVerifier(publicKey);
        if (!signedJWT.verify(verifier)) {
            throw new SecurityException("JWT signature verification failed");
        }

        // Extract claims
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        // Validate issuer
        if (!properties.getIssuer().equals(claims.getIssuer())) {
            throw new SecurityException("Invalid issuer: " + claims.getIssuer());
        }

        // Validate expiration
        Date expiration = claims.getExpirationTime();
        if (expiration == null || expiration.before(Date.from(Instant.now()))) {
            throw new SecurityException("Token expired");
        }

        // Extract user details
        String userId = claims.getSubject();
        String username = claims.getStringClaim("username");
        String role = claims.getStringClaim("roles");

        // Create authorities
        List<SimpleGrantedAuthority> authorities = role != null
                ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                : Collections.emptyList();

        return new JwtAuthenticationToken(userId, username, token, authorities);
    }
}
