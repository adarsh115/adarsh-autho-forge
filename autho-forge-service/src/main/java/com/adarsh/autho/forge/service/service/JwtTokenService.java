package com.adarsh.autho.forge.service.service;

import com.adarsh.autho.forge.service.entity.AuthUser;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenService {

    @Autowired
    private KeyProviderService keyProviderService;

    @Value("${autho.forge.iss}")
    private String issuer;

    @Value("${autho.forge.access-token.ttl-minutes}")
    private long accessTokenTtlMinutes;

    public String generateAccessToken(AuthUser user) {
        //Build JWT claims/payload
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("roles", user.getRole().name())
                .issuer(issuer)
                .issueTime(new Date())
                .expirationTime(
                        Date.from(Instant.now()
                                .plus(Duration.ofMinutes(accessTokenTtlMinutes))))
                        .build();

        //Builder JWT header with RSA256 and Key ID
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(keyProviderService.getKeyId())
                .type(JOSEObjectType.JWT)
                .build();

        //Create the signed JWT Object
        SignedJWT signedJWT = new SignedJWT(header, claims);

        //Sign using RSA private key
        try {
            RSASSASigner signer = new RSASSASigner(keyProviderService.getPrivateKey());
            signedJWT.sign(signer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }

        //Return token string
        return signedJWT.serialize();
    }

}