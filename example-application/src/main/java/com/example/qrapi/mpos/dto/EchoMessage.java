package com.example.qrapi.mpos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Echo Request/Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EchoMessage {
    
    @JsonProperty("echo")
    private String echo; // Message to echo
}
