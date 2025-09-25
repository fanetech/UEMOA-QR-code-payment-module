package com.example.qrapi.mpos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Key Exchange Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyExchangeResponse {
    
    @JsonProperty("jwt.pub")
    private String jwtPublicKey; // JWT Public Key in PEM format
    
    @JsonProperty("jwt.prv")
    private String jwtPrivateKey; // JWT Private Key in PEM format
    
    @JsonProperty("mtls.crt")
    private String mtlsCertificate; // mTLS Certificate in PEM format
    
    @JsonProperty("mtls.key")
    private String mtlsPrivateKey; // mTLS Private Key in PEM format
}
