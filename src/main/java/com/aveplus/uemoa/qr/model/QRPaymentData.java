package com.aveplus.uemoa.qr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Données de paiement pour la génération du QR code
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRPaymentData {
    
    /**
     * Type de QR code
     */
    public enum QRType {
        /**
         * QR statique pour marchand (avec ou sans montant)
         */
        STATIC,
        
        /**
         * QR dynamique généré pour chaque transaction
         */
        DYNAMIC,
        
        /**
         * QR pour transfert entre particuliers
         */
        P2P
    }
    
    /**
     * Canaux marchands selon la spécification BCEAO
     */
    public enum MerchantChannel {
        // Canaux statiques
        STATIC_ONSITE(100, "QR statique sur site"),
        STATIC_WITH_AMOUNT(110, "QR statique avec montant"),
        STATIC_WITH_TXID(120, "QR statique avec ID transaction"),
        STATIC_INVOICE(131, "QR statique sur facture"),
        
        // Canaux dynamiques
        DYNAMIC_ONSITE(500, "QR dynamique sur site"),
        DYNAMIC_ECOMMERCE_WEB(521, "QR dynamique e-commerce Web"),
        DYNAMIC_ECOMMERCE_APP(522, "QR dynamique e-commerce App"),
        
        // Canal P2P
        P2P_STATIC(731, "QR statique pour particulier");
        
        private final int code;
        private final String description;
        
        MerchantChannel(int code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static MerchantChannel fromCode(int code) {
            for (MerchantChannel channel : values()) {
                if (channel.code == code) {
                    return channel;
                }
            }
            return null;
        }
    }
    
    /**
     * Type de QR code à générer
     */
    private QRType type = QRType.STATIC;
    
    /**
     * Informations du marchand ou du particulier
     */
    @Valid
    private MerchantInfo merchantInfo;
    
    /**
     * Montant de la transaction (optionnel pour QR statique)
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;
    
    /**
     * Identifiant de transaction
     */
    @Pattern(regexp = "^[A-Za-z0-9-]{1,25}$", 
             message = "L'ID de transaction ne doit contenir que des caractères alphanumériques et tirets (max 25)")
    private String transactionId;
    
    /**
     * Référence de facture
     */
    @Pattern(regexp = "^[A-Za-z0-9-]{1,25}$", 
             message = "La référence de facture ne doit contenir que des caractères alphanumériques et tirets (max 25)")
    private String billReference;
    
    /**
     * Identifiant d'abonnement
     */
    @Pattern(regexp = "^[A-Za-z0-9-]{1,25}$", 
             message = "L'ID d'abonnement ne doit contenir que des caractères alphanumériques et tirets (max 25)")
    private String subscriptionId;
    
    /**
     * Canal marchand
     */
    private MerchantChannel merchantChannel;
    
    /**
     * URL dynamique (pour QR dynamique)
     */
    private String dynamicUrl;
    
    /**
     * Données additionnelles
     */
    @Builder.Default
    private Map<String, String> additionalData = new HashMap<>();
    
    /**
     * Méthode d'initiation du paiement
     */
    public String getPointOfInitiationMethod() {
        return type == QRType.DYNAMIC ? "12" : "11";
    }
}
