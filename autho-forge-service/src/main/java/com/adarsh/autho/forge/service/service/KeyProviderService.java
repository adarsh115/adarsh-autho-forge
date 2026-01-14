package com.adarsh.autho.forge.service.service;

import com.adarsh.autho.forge.service.config.KeyProperties;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;

@Service
public class KeyProviderService {

    @Autowired
    private KeyProperties keyProperties;

    @Autowired
    private ResourceLoader resourceLoader;

    private RSAPrivateKey privateKey;

    @PostConstruct
    public void loadKey() {
        try {
            // Ensure BouncyCastle is registered
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            String pemContent;
            
            if (keyProperties.getContent() != null && !keyProperties.getContent().isBlank()) {
                // Load from direct content (Env Var)
                System.out.println("📝 Loading private key from configuration 'content' property...");
                // Handle potential Base64 encoding if user provided it as such, but standard PEM string is fine too
                // If it starts with "LS0t", it's likely Base64 encoded PEM
                String raw = keyProperties.getContent().trim();
                if (!raw.startsWith("-----")) {
                     try {
                         byte[] decoded = java.util.Base64.getDecoder().decode(raw);
                         pemContent = new String(decoded, StandardCharsets.UTF_8);
                     } catch (IllegalArgumentException e) {
                         // Not base64, assume raw text
                         pemContent = raw;
                     }
                } else {
                    pemContent = raw;
                }
            } else {
                // Fallback to file path
                System.out.println("Rx Loading private key from file path: " + keyProperties.getPath());
                Resource resource = resourceLoader.getResource(keyProperties.getPath());
                try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    pemContent = FileCopyUtils.copyToString(reader);
                }
            }

            this.privateKey = parsePrivateKey(pemContent);
            if (this.privateKey == null) {
                throw new IllegalStateException("Failed to parse RSA private key (content or path)");
            }

            System.out.println("✔ RSA private key loaded successfully (kid=" + keyProperties.getKid() + ")");

        } catch (IOException | OperatorCreationException e) {
            throw new IllegalStateException("Failed to load RSA private key", e);
        }
    }

    private RSAPrivateKey parsePrivateKey(String pemContent)
            throws IOException, OperatorCreationException {

        try (PEMParser pemParser = new PEMParser(new StringReader(pemContent))) {
            Object parsed = pemParser.readObject();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (parsed instanceof PrivateKeyInfo privateKeyInfo) {
                // Unencrypted PKCS#8
                PrivateKey pk = converter.getPrivateKey(privateKeyInfo);
                return (RSAPrivateKey) pk;

            } else if (parsed instanceof PEMKeyPair keyPair) {
                // Unencrypted PKCS#1 key pair
                PrivateKeyInfo privateKeyInfo = keyPair.getPrivateKeyInfo();
                PrivateKey pk = converter.getPrivateKey(privateKeyInfo);
                return (RSAPrivateKey) pk;

            } else if (parsed instanceof PKCS8EncryptedPrivateKeyInfo encInfo) {
                // Encrypted PKCS#8
                char[] passphrase = getPassphraseChars();
                InputDecryptorProvider decryptorProvider =
                        new JceOpenSSLPKCS8DecryptorProviderBuilder()
                                .build(passphrase);

                PrivateKeyInfo privateKeyInfo = encInfo.decryptPrivateKeyInfo(decryptorProvider);
                PrivateKey pk = converter.getPrivateKey(privateKeyInfo);
                return (RSAPrivateKey) pk;

            } else if (parsed instanceof PEMEncryptedKeyPair encKeyPair) {
                // Legacy OpenSSL encrypted key pair
                char[] passphrase = getPassphraseChars();
                var decProv = new JcePEMDecryptorProviderBuilder().build(passphrase);
                PEMKeyPair keyPair = ((PEMEncryptedKeyPair) parsed).decryptKeyPair(decProv);
                PrivateKeyInfo privateKeyInfo = keyPair.getPrivateKeyInfo();
                PrivateKey pk = converter.getPrivateKey(privateKeyInfo);
                return (RSAPrivateKey) pk;

            } else {
                throw new IllegalStateException("Unsupported key format in PEM");
            }
        } catch (PKCSException e) {
            throw new RuntimeException(e);
        }
    }

    private char[] getPassphraseChars() {
        String passphrase = keyProperties.getPassphrase();
        if (passphrase == null || passphrase.isBlank()) {
            throw new IllegalStateException("Encrypted key detected but no passphrase configured (autho.forge.key.passphrase)");
        }
        return passphrase.toCharArray();
    }

    public RSAPrivateKey getPrivateKey() {
        if (privateKey == null) {
            throw new IllegalStateException("Private key not initialized");
        }
        return privateKey;
    }
    public String getKeyId() {
        return keyProperties.getKid();
    }

    public java.security.interfaces.RSAPublicKey getPublicKey() {
        if (privateKey instanceof java.security.interfaces.RSAPrivateCrtKey) {
            java.security.interfaces.RSAPrivateCrtKey crtKey = (java.security.interfaces.RSAPrivateCrtKey) privateKey;
            try {
                java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
                java.security.spec.RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(
                    crtKey.getModulus(),
                    crtKey.getPublicExponent()
                );
                return (java.security.interfaces.RSAPublicKey) kf.generatePublic(publicKeySpec);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to derive public key", e);
            }
        }
        throw new IllegalStateException("Cannot derive public key: private key is not an RSAPrivateCrtKey");
    }
}
