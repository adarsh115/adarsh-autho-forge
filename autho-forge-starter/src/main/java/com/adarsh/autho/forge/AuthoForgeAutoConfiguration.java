package com.adarsh.autho.forge;

import com.adarsh.autho.forge.config.AuthoForgeProperties;
import com.adarsh.autho.forge.filter.JwtAuthenticationFilter;
import com.adarsh.autho.forge.service.JwkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Auto-configuration for Autho-Forge JWT authentication.
 * Automatically configures JWT validation when autho.forge.enabled=true
 */
@AutoConfiguration
@EnableWebSecurity
@EnableConfigurationProperties(AuthoForgeProperties.class)
@ConditionalOnProperty(prefix = "autho.forge", name = "enabled", havingValue = "true")
public class AuthoForgeAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AuthoForgeAutoConfiguration.class);

    public AuthoForgeAutoConfiguration() {
        log.info("🔐 Autho-Forge authentication enabled");
    }

    @Bean
    public JwkService jwkService(AuthoForgeProperties properties) {
        log.info("Initializing JWK service with URI: {}", properties.getJwkSetUri());
        return new JwkService(properties);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwkService jwkService,
            AuthoForgeProperties properties) {
        return new JwtAuthenticationFilter(jwkService, properties);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/health/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("✅ Security filter chain configured with JWT authentication");
        return http.build();
    }
}