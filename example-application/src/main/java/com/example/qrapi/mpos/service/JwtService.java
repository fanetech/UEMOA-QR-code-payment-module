package com.example.qrapi.mpos.service;

import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

/**
 * Service for handling JWT operations (JWS, JWE, and Nested JWT)
 */
@Slf4j
@Service
public class JwtService {

    /**
     * Create a JWE (JSON Web Encryption) token
     */
    public String createJWE(Map<String, Object> payload, PublicKey publicKey) throws JoseException {
        JsonWebEncryption jwe = new JsonWebEncryption();
        
        // Set the payload
        JwtClaims claims = new JwtClaims();
        payload.forEach(claims::setClaim);
        claims.setExpirationTimeMinutesInTheFuture(120); // 2 hours
        claims.setIssuedAtToNow();
        
        jwe.setPayload(claims.toJson());
        jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
        jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_256_GCM);
        jwe.setContentTypeHeaderValue("JWT");
        jwe.setKey(publicKey);
        
        return jwe.getCompactSerialization();
    }

    /**
     * Create a JWS (JSON Web Signature) token
     */
    public String createJWS(Map<String, Object> payload, PrivateKey privateKey) throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        
        // Set the payload
        JwtClaims claims = new JwtClaims();
        payload.forEach(claims::setClaim);
        claims.setExpirationTimeMinutesInTheFuture(120); // 2 hours
        claims.setIssuedAtToNow();
        
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setKey(privateKey);
        jws.setKeyIdHeaderValue("1");
        
        return jws.getCompactSerialization();
    }

    /**
     * Create a Nested JWT (JWS wrapped in JWE)
     */
    public String createNestedJWT(Map<String, Object> payload, PrivateKey signingKey, PublicKey encryptionKey) 
            throws JoseException {
        // First create the signed JWT
        String jws = createJWS(payload, signingKey);
        
        // Then encrypt the signed JWT
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setPayload(jws);
        jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
        jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_256_GCM);
        jwe.setContentTypeHeaderValue("JWT"); // Important for nested JWT
        jwe.setKey(encryptionKey);
        
        return jwe.getCompactSerialization();
    }

    /**
     * Decrypt a JWE token
     */
    public Map<String, Object> decryptJWE(String jwe, PrivateKey privateKey) throws JoseException {
        JsonWebEncryption jsonWebEncryption = new JsonWebEncryption();
        jsonWebEncryption.setCompactSerialization(jwe);
        jsonWebEncryption.setKey(privateKey);
        
        String payload = jsonWebEncryption.getPayload();
        
        // Parse the payload as JWT claims
        JwtClaims claims = JwtClaims.parse(payload);
        return claims.getClaimsMap();
    }

    /**
     * Verify a JWS token
     */
    public Map<String, Object> verifyJWS(String jws, PublicKey publicKey) throws JoseException {
        JsonWebSignature jsonWebSignature = new JsonWebSignature();
        jsonWebSignature.setCompactSerialization(jws);
        jsonWebSignature.setKey(publicKey);
        
        if (!jsonWebSignature.verifySignature()) {
            throw new JoseException("Invalid signature");
        }
        
        String payload = jsonWebSignature.getPayload();
        JwtClaims claims = JwtClaims.parse(payload);
        return claims.getClaimsMap();
    }

    /**
     * Process a Nested JWT (decrypt then verify)*
     */
    public Map<String, Object> processNestedJWT(String nestedJWT, PrivateKey decryptionKey, PublicKey verificationKey) 
            throws JoseException {
        // First decrypt the outer JWE
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setCompactSerialization(nestedJWT);
        jwe.setKey(decryptionKey);
        
        String innerJWS = jwe.getPayload();
        
        // Then verify the inner JWS
        return verifyJWS(innerJWS, verificationKey);
    }

    /**
     * Convert PEM string to PublicKey
     */
    public PublicKey pemToPublicKey(String pemString) throws Exception {
        String publicKeyPEM = pemString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * Convert PEM string to PrivateKey
     */
    public PrivateKey pemToPrivateKey(String pemString) throws Exception {
        String privateKeyPEM = pemString
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * Generate RSA key pair
     */
    public KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    /**
     * Convert PublicKey to PEM format
     */
    public String publicKeyToPEM(PublicKey publicKey) {
        Base64.Encoder encoder = Base64.getMimeEncoder(64, "\n".getBytes());
        byte[] encoded = publicKey.getEncoded();
        String encodedString = encoder.encodeToString(encoded);
        return "-----BEGIN PUBLIC KEY-----\n" + encodedString + "\n-----END PUBLIC KEY-----\n";
    }

    /**
     * Convert PrivateKey to PEM format
     */
    public String privateKeyToPEM(PrivateKey privateKey) {
        Base64.Encoder encoder = Base64.getMimeEncoder(64, "\n".getBytes());
        byte[] encoded = privateKey.getEncoded();
        String encodedString = encoder.encodeToString(encoded);
        return "-----BEGIN RSA PRIVATE KEY-----\n" + encodedString + "\n-----END RSA PRIVATE KEY-----\n";
    }
}
