package com.aveplus.uemoa.qr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriétés de configuration pour le module UEMOA QR Payment
 */
@Data
@ConfigurationProperties(prefix = "uemoa.qr.payment")
public class UemoaQrProperties {
    
    /**
     * Code pays par défaut (CI = Côte d'Ivoire)
     */
    private String defaultCountryCode = "CI";
    
    /**
     * Code devise (952 = XOF - Franc CFA BCEAO)
     */
    private String currencyCode = "952";
    
    /**
     * Indicateur de format de payload (toujours "01" selon EMVCo)
     */
    private String payloadFormatIndicator = "01";
    
    /**
     * Code catégorie marchand ("0000" pour tous selon BCEAO)
     */
    private String merchantCategoryCode = "0000";
    
    /**
     * Préfixe BCEAO pour l'identification
     */
    private String bceaoPrefix = "int.bceao.pi";
    
    /**
     * Active la validation du CRC lors du parsing
     */
    private boolean validateCrc = true;
    
    /**
     * Active la génération d'images QR
     */
    private boolean generateQrImage = true;
    
    /**
     * Taille par défaut des images QR (en pixels)
     */
    private int qrImageSize = 300;
    
    /**
     * Marge des images QR (en pixels)
     */
    private int qrImageMargin = 1;
    
    /**
     * Format d'image par défaut (PNG, JPG)
     */
    private String imageFormat = "PNG";
    
    /**
     * Active les logs détaillés
     */
    private boolean debugMode = false;
}
