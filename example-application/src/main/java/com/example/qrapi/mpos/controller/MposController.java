package com.example.qrapi.mpos.controller;

import com.example.qrapi.mpos.dto.EchoMessage;
import com.example.qrapi.mpos.dto.KeyExchangeRequest;
import com.example.qrapi.mpos.dto.KeyExchangeResponse;
import com.example.qrapi.mpos.service.MposService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * MPOS API Controller
 * Handles JWT and mTLS secured endpoints
 */
@Slf4j
@RestController
@RequestMapping("/avepay-mpos-main/avepay")
@RequiredArgsConstructor
@Tag(name = "MPOS API", description = "MPOS API endpoints with JWT and mTLS security")
public class MposController {

    private final MposService mposService;
    private final ObjectMapper objectMapper;
    
    private static final String JWT_CONTENT_TYPE = "application/jwt";

    /**
     * Key Exchange endpoint
     * Security: JWE request, JWS response
     */
    @PostMapping(value = "/auth/keyexchange", 
                 consumes = {MediaType.APPLICATION_JSON_VALUE, JWT_CONTENT_TYPE},
                 produces = {MediaType.APPLICATION_JSON_VALUE, JWT_CONTENT_TYPE})
    @Operation(
        summary = "Key Exchange",
        description = "Exchange device details for JWT keys and mTLS certificates. Request uses JWE, response uses JWS.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successful key exchange",
                content = @Content(schema = @Schema(implementation = KeyExchangeResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Server error")
        }
    )
    public ResponseEntity<?> keyExchange(
            @RequestBody @Valid KeyExchangeRequest request,
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, defaultValue = MediaType.APPLICATION_JSON_VALUE) String contentType) {
        
        try {
            log.info("Key exchange request received for device: {}", request.getCn());
            
            // Process key exchange
            KeyExchangeResponse response = mposService.processKeyExchange(request);
            
            // Return as JWS if JWT content type is requested
            if (JWT_CONTENT_TYPE.equals(contentType)) {
                String jws = mposService.createKeyExchangeJWS(response);
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, JWT_CONTENT_TYPE)
                    .body(jws);
            }
            
            // Return as JSON for testing purposes
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Key exchange failed", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Echo endpoint for testing JWT and mTLS
     * Security: Nested JWT for both request and response
     */
    @PostMapping(value = "/auth/echo",
                 consumes = {MediaType.APPLICATION_JSON_VALUE, JWT_CONTENT_TYPE},
                 produces = {MediaType.APPLICATION_JSON_VALUE, JWT_CONTENT_TYPE})
    @Operation(
        summary = "Echo Test",
        description = "Test endpoint for JWT and mTLS setup. Uses nested JWT (JWS+JWE) for both request and response.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Echo successful",
                content = @Content(schema = @Schema(implementation = EchoMessage.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
        }
    )
    public ResponseEntity<?> echo(
            @RequestBody @Valid EchoMessage request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, defaultValue = MediaType.APPLICATION_JSON_VALUE) String contentType) {
        
        try {
            log.info("Echo request received: {}", request.getEcho());
            
            // Process echo
            EchoMessage response = mposService.processEcho(request, deviceId);
            
            // Return as Nested JWT if JWT content type is requested and device ID is provided
            if (JWT_CONTENT_TYPE.equals(contentType) && deviceId != null) {
                String nestedJWT = mposService.createNestedJWT(response, deviceId);
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, JWT_CONTENT_TYPE)
                    .body(nestedJWT);
            }
            
            // Return as JSON for testing purposes
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Echo failed", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get server certificate for initial setup
     */
    @GetMapping("/auth/server-certificate")
    @Operation(
        summary = "Get Server Certificate",
        description = "Get the server's public certificate in PEM format for initial Key Exchange setup",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Server certificate retrieved successfully",
                content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
            ),
            @ApiResponse(responseCode = "500", description = "Server error")
        }
    )
    public ResponseEntity<?> getServerCertificate() {
        try {
            String certificatePEM = mposService.getServerCertificatePEM();
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(certificatePEM);
        } catch (Exception e) {
            log.error("Failed to get server certificate", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get server public key for testing
     */
    @GetMapping("/auth/server-public-key")
    @Operation(
        summary = "Get Server Public Key",
        description = "Get the server's public key in PEM format for testing purposes",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Server public key retrieved successfully",
                content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
            ),
            @ApiResponse(responseCode = "500", description = "Server error")
        }
    )
    public ResponseEntity<?> getServerPublicKey() {
        try {
            String publicKeyPEM = mposService.getServerPublicKeyPEM();
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(publicKeyPEM);
        } catch (Exception e) {
            log.error("Failed to get server public key", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Test JWT creation endpoint
     */
    @PostMapping("/test/create-jwt")
    @Operation(
        summary = "Test JWT Creation",
        description = "Test endpoint to create different types of JWT tokens",
        responses = {
            @ApiResponse(responseCode = "200", description = "JWT created successfully")
        }
    )
    public ResponseEntity<?> testCreateJWT(
            @RequestParam(defaultValue = "NESTED") @Parameter(description = "JWT type: JWE, JWS, or NESTED") String type,
            @RequestBody Map<String, Object> payload,
            @RequestParam(required = false) @Parameter(description = "Device ID for nested JWT") String deviceId) {
        
        try {
            String jwt;
            switch (type.toUpperCase()) {
                case "JWE":
                    // Create JWE with server public key
                    mposService.initializeServerKeys();
                    jwt = mposService.createKeyExchangeJWE(
                        objectMapper.convertValue(payload, KeyExchangeRequest.class)
                    );
                    break;
                case "JWS":
                    // Create JWS with response data
                    KeyExchangeResponse response = objectMapper.convertValue(payload, KeyExchangeResponse.class);
                    jwt = mposService.createKeyExchangeJWS(response);
                    break;
                case "NESTED":
                    // Create Nested JWT
                    if (deviceId == null) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", "Device ID required for nested JWT"));
                    }
                    jwt = mposService.createNestedJWT(payload, deviceId);
                    break;
                default:
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid JWT type. Use JWE, JWS, or NESTED"));
            }
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, JWT_CONTENT_TYPE)
                .body(jwt);
                
        } catch (Exception e) {
            log.error("JWT creation failed", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Test JWT processing endpoint
     */
    @PostMapping("/test/process-jwt")
    @Operation(
        summary = "Test JWT Processing",
        description = "Test endpoint to decrypt and verify JWT tokens",
        responses = {
            @ApiResponse(responseCode = "200", description = "JWT processed successfully")
        }
    )
    public ResponseEntity<?> testProcessJWT(
            @RequestBody String jwt,
            @RequestParam(defaultValue = "NESTED") @Parameter(description = "JWT type: JWE, JWS, or NESTED") String type,
            @RequestParam(required = false) @Parameter(description = "Device ID for nested JWT") String deviceId) {
        
        try {
            Map<String, Object> payload;
            
            switch (type.toUpperCase()) {
                case "NESTED":
                    if (deviceId == null) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", "Device ID required for nested JWT"));
                    }
                    payload = mposService.processNestedJWT(jwt, deviceId);
                    break;
                default:
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Processing type not yet implemented"));
            }
            
            return ResponseEntity.ok(payload);
                
        } catch (Exception e) {
            log.error("JWT processing failed", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health Check",
        description = "Check if the MPOS API is running",
        responses = {
            @ApiResponse(responseCode = "200", description = "Service is healthy")
        }
    )
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "MPOS API",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}
