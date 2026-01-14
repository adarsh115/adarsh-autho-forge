package com.adarsh.autho.forge.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "autho.forge.key")
@Getter
@Setter
public class KeyProperties {
    /**
     * Path to the encrypted private key file (.pem or .pem.enc)
     * Example: classpath:keys/private.pem.enc
     */
//    @Value("${autho.forge.key.path}")
    private String path;

    /**
     * Passphrase for decrypting the key (should come from environment variable in prod)
     */
//    @Value("${autho.forge.key.passphrase}")
    private String passphrase;

    /**
     * Key ID used in JWT header ("kid")
     */
//    @Value("${autho.forge.key.kid}") - using value with configuration is wrong
    private String kid;
}
