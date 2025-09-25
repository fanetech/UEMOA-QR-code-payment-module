package com.example.qrapi.mpos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Key Exchange Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyExchangeRequest {
    
    @NotBlank(message = "Device name (cn) is required")
    @JsonProperty("cn")
    private String cn; // Device Name e.g. mpos-12345
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @JsonProperty("email")
    private String email; // Email Address e.g. mpos@aveplus.net
    
    @NotBlank(message = "Mobile number (tel) is required")
    @JsonProperty("tel")
    private String tel; // Mobile number e.g. 22600000333
    
    @NotBlank(message = "Serial number (sn) is required")
    @JsonProperty("sn")
    private String sn; // Mobile Device Serial Number e.g. 80052448348015
    
    @NotBlank(message = "UUID (uid) is required")
    @JsonProperty("uid")
    private String uid; // UUID e.g. ff175490-fc1a-4315-aa9c-30f088bacebf
}
