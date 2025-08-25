package com.example.qrapi.controller;

import com.aveplus.uemoa.qr.model.MerchantInfo;
import com.aveplus.uemoa.qr.model.QRPaymentData;
import com.aveplus.uemoa.qr.service.UemoaQRService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * ⭐ VOICI LE CONTROLLER QUI UTILISE VOTRE MODULE !
 * 
 * Il importe et utilise UemoaQRService de votre module
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor
@Tag(name = "QR Code API", description = "Endpoints pour générer et parser des QR codes UEMOA")
@CrossOrigin(origins = "*")
public class QRCodeController {

    // ⭐ Injection du service de VOTRE MODULE
    private final UemoaQRService qrService;

    @PostMapping("/generate")
    @Operation(summary = "Générer un QR code")
    public ResponseEntity<Map<String, Object>> generateQR(
            @Valid @RequestBody QRPaymentData request) {
        
        try {
            String qrData = qrService.generateQRData(request);
            String qrImageBase64 = qrService.generateQRImage(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", qrData);
            response.put("image", qrImageBase64);
            response.put("type", request.getType());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/parse")
    @Operation(summary = "Parser un QR code")
    public ResponseEntity<QRPaymentData> parseQR(@RequestBody Map<String, String> request) {
        String qrData = request.get("qrData");
        QRPaymentData parsed = qrService.parseQRCode(qrData);
        return ResponseEntity.ok(parsed);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "UEMOA QR API");
        return ResponseEntity.ok(status);
    }
}
