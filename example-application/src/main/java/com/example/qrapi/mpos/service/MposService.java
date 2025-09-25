package com.example.qrapi.mpos.service;

import com.example.qrapi.mpos.dto.EchoMessage;
import com.example.qrapi.mpos.dto.KeyExchangeRequest;
import com.example.qrapi.mpos.dto.KeyExchangeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main MPOS API Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MposService {

    private final JwtService jwtService;
    private final CertificateService certificateService;
    
    // In-memory storage for demo purposes (use database in production)
    private final Map<String, DeviceKeys> deviceKeysStore = new ConcurrentHashMap<>();
    
    // Server keys (should be loaded from secure storage in production)
    private KeyPair serverKeyPair;
    private X509Certificate serverCertificate;
    
    /**
     * Initialize server keys
     */
    public void initializeServerKeys() throws Exception {
        if (serverKeyPair == null) {
            serverKeyPair = certificateService.generateRSAKeyPair(2048);
            serverCertificate = certificateService.generateSelfSignedCertificate(
                serverKeyPair, "mpos.avepay.net"
            );
            log.info("Server keys initialized successfully");
        }
    }
    
    /**
     * Process Key Exchange request
     */
    public KeyExchangeResponse processKeyExchange(KeyExchangeRequest request) throws Exception {
        log.info("Processing key exchange for device: {}", request.getCn());
        
        // Generate new keys for the device
        KeyPair jwtKeyPair = certificateService.generateRSAKeyPair(2048);
        KeyPair mtlsKeyPair = certificateService.generateRSAKeyPair(2048);
        
        // Generate mTLS certificate for the device
        X509Certificate mtlsCert = certificateService.generateSelfSignedCertificate(
            mtlsKeyPair, request.getCn()
        );
        
        // Store device keys for future use
        DeviceKeys deviceKeys = new DeviceKeys();
        deviceKeys.deviceId = request.getUid();
        deviceKeys.jwtPublicKey = jwtKeyPair.getPublic();
        deviceKeys.jwtPrivateKey = jwtKeyPair.getPrivate();
        deviceKeys.mtlsPublicKey = mtlsKeyPair.getPublic();
        deviceKeys.mtlsPrivateKey = mtlsKeyPair.getPrivate();
        deviceKeys.mtlsCertificate = mtlsCert;
        
        deviceKeysStore.put(request.getUid(), deviceKeys);
        
        // Build response with PEM formatted keys
        return KeyExchangeResponse.builder()
            .jwtPublicKey(certificateService.publicKeyToPEM(jwtKeyPair.getPublic()))
            .jwtPrivateKey(certificateService.privateKeyToPEM(jwtKeyPair.getPrivate()))
            .mtlsCertificate(certificateService.certificateToPEM(mtlsCert))
            .mtlsPrivateKey(certificateService.privateKeyToPEM(mtlsKeyPair.getPrivate()))
            .build();
    }
    
    /**
     * Process Echo request (for testing JWT and mTLS)
     */
    public EchoMessage processEcho(EchoMessage request, String deviceId) {
        log.info("Processing echo request from device: {}", deviceId);
        return EchoMessage.builder()
            .echo(request.getEcho())
            .build();
    }
    
    /**
     * Create JWE for Key Exchange response
     */
    public String createKeyExchangeJWE(KeyExchangeRequest request) throws Exception {
        initializeServerKeys();
        Map<String, Object> payload = new HashMap<>();
        payload.put("cn", request.getCn());
        payload.put("email", request.getEmail());
        payload.put("tel", request.getTel());
        payload.put("sn", request.getSn());
        payload.put("uid", request.getUid());
        
        return jwtService.createJWE(payload, serverKeyPair.getPublic());
    }
    
    /**
     * Create JWS for Key Exchange response
     */
    public String createKeyExchangeJWS(KeyExchangeResponse response) throws Exception {
        initializeServerKeys();
        Map<String, Object> payload = new HashMap<>();
        payload.put("jwt.pub", response.getJwtPublicKey());
        payload.put("jwt.prv", response.getJwtPrivateKey());
        payload.put("mtls.crt", response.getMtlsCertificate());
        payload.put("mtls.key", response.getMtlsPrivateKey());
        
        return jwtService.createJWS(payload, serverKeyPair.getPrivate());
    }
    
    /**
     * Create Nested JWT for regular API responses
     */
    public String createNestedJWT(Object payload, String deviceId) throws Exception {
        DeviceKeys deviceKeys = deviceKeysStore.get(deviceId);
        if (deviceKeys == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("data", payload);
        claims.put("deviceId", deviceId);
        
        return jwtService.createNestedJWT(
            claims,
            deviceKeys.jwtPrivateKey,
            deviceKeys.jwtPublicKey
        );
    }
    
    /**
     * Process Nested JWT from request
     */
    public Map<String, Object> processNestedJWT(String nestedJWT, String deviceId) throws Exception {
        DeviceKeys deviceKeys = deviceKeysStore.get(deviceId);
        if (deviceKeys == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }
        
        return jwtService.processNestedJWT(
            nestedJWT,
            deviceKeys.jwtPrivateKey,
            deviceKeys.jwtPublicKey
        );
    }
    
    /**
     * Get server certificate in PEM format
     */
    public String getServerCertificatePEM() throws Exception {
        initializeServerKeys();
        return certificateService.certificateToPEM(serverCertificate);
    }
    
    /**
     * Get server public key in PEM format
     */
    public String getServerPublicKeyPEM() throws Exception {
        initializeServerKeys();
        return certificateService.publicKeyToPEM(serverKeyPair.getPublic());
    }
    
    /**
     * Inner class to store device keys
     */
    private static class DeviceKeys {
        String deviceId;
        PublicKey jwtPublicKey;
        PrivateKey jwtPrivateKey;
        PublicKey mtlsPublicKey;
        PrivateKey mtlsPrivateKey;
        X509Certificate mtlsCertificate;
    }
}
