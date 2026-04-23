package com.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class JwtTokenProvider {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final String issuer;
    private final long expirationMinutes;

    public JwtTokenProvider(@Value("${jwt.private-key-path}") String privateKeyPath,
                            @Value("${jwt.public-key-path}") String publicKeyPath,
                            @Value("${jwt.issuer}") String issuer,
                            @Value("${jwt.expiration-minutes}") long expirationMinutes) throws Exception {
        this.privateKey = loadRsaPrivateKey(privateKeyPath);
        this.publicKey = loadRsaPublicKey(publicKeyPath);
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(authentication.getName())
                .issuer(issuer)
                .issueTime(new Date(now.toEpochMilli()))
                .expirationTime(new Date(now.plus(expirationMinutes, java.time.temporal.ChronoUnit.MINUTES).toEpochMilli()))
                .claim("roles", authentication.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("auth-service-key-1").build(),
                claims
        );
        try {
            signedJWT.sign(new RSASSASigner(privateKey));
        } catch (JOSEException e) {
            throw new RuntimeException("JWT signing failed", e);
        }
        return signedJWT.serialize();
    }

    public String getJwksJson() {
        try {
            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .keyID("auth-service-key-1")
                    .algorithm(JWSAlgorithm.RS256)
                    .build();
            return new ObjectMapper().writeValueAsString(new JWKSet(rsaKey).toJSONObject());
        } catch (Exception e) {
            throw new RuntimeException("JWK generation failed", e);
        }
    }

    private String readPemResource(String path) throws Exception {
        Resource resource;
        if (path.startsWith("classpath:")) {
            resource = new ClassPathResource(path.substring("classpath:".length()));
        } else {
            resource = new FileSystemResource(path);
        }
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private RSAPrivateKey loadRsaPrivateKey(String path) throws Exception {
        String pem = readPemResource(path);
        String cleaned = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(cleaned);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private RSAPublicKey loadRsaPublicKey(String path) throws Exception {
        String pem = readPemResource(path);
        String cleaned = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(cleaned);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(decoded));
    }
}
