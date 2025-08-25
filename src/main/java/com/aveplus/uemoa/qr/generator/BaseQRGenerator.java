package com.aveplus.uemoa.qr.generator;

import com.aveplus.uemoa.qr.config.UemoaQrProperties;
import com.aveplus.uemoa.qr.model.QRPaymentData;
import com.aveplus.uemoa.qr.utils.CRCCalculator;
import com.aveplus.uemoa.qr.utils.EMVFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.TreeMap;

/**
 * Classe de base pour la génération de QR codes EMVCo
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseQRGenerator {
    
    protected final EMVFormatter formatter;
    protected final CRCCalculator crcCalculator;
    protected final UemoaQrProperties properties;
    
    /**
     * Champs EMV pour la génération du QR
     */
    protected Map<String, String> fields;
    
    /**
     * Initialise les champs par défaut selon la spécification BCEAO
     */
    protected void initializeDefaults() {
        fields = new TreeMap<>();
        fields.put("00", properties.getPayloadFormatIndicator()); // Toujours "01"
        fields.put("52", properties.getMerchantCategoryCode()); // "0000" pour tous
        fields.put("53", properties.getCurrencyCode()); // "952" pour XOF
    }
    
    /**
     * Configure les informations du compte marchand
     * 
     * @param alias L'alias/proxy du compte
     */
    protected void setMerchantAccountInfo(String alias) {
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("L'alias du compte est obligatoire");
        }
        
        // Format: 36 + longueur + (00 + longueur + "int.bceao.pi") + (01 + longueur + alias)
        String merchantInfo = 
            formatter.formatSubField("00", properties.getBceaoPrefix()) +
            formatter.formatSubField("01", alias);
        
        fields.put("36", merchantInfo);
        log.debug("Merchant account info configuré: {}", merchantInfo);
    }
    
    /**
     * Configure les informations du compte avec une URL dynamique
     * 
     * @param url L'URL dynamique du PSP
     */
    protected void setDynamicMerchantInfo(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("L'URL dynamique est obligatoire");
        }
        
        // Pour QR dynamique, on peut utiliser une URL directement
        String merchantInfo = 
            formatter.formatSubField("00", properties.getBceaoPrefix()) +
            formatter.formatSubField("02", url);
        
        fields.put("36", merchantInfo);
        log.debug("Dynamic merchant info configuré avec URL: {}", url);
    }
    
    /**
     * Configure les données additionnelles (champ 62)
     * 
     * @param data Map des données additionnelles
     */
    protected void setAdditionalData(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        
        StringBuilder additionalData = new StringBuilder();
        
        data.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                additionalData.append(formatter.formatSubField(key, value));
            }
        });
        
        if (additionalData.length() > 0) {
            fields.put("62", additionalData.toString());
            log.debug("Données additionnelles configurées: {}", additionalData);
        }
    }
    
    /**
     * Construit la chaîne finale du QR code avec CRC
     * 
     * @return La chaîne QR code complète
     */
    protected String buildQRString() {
        StringBuilder qrData = new StringBuilder();
        
        // Construit tous les champs sauf le CRC
        fields.entrySet().stream()
            .filter(e -> !"63".equals(e.getKey()))
            .forEach(e -> {
                String formatted = formatter.formatField(e.getKey(), e.getValue());
                qrData.append(formatted);
            });
        
        // Ajoute le placeholder pour le CRC (63 + 04)
        qrData.append("6304");
        
        // Calcule le CRC sur toutes les données incluant "6304"
        String crc = crcCalculator.calculate(qrData.toString());
        
        // Remplace les 4 derniers caractères par le CRC calculé
        String result = qrData.substring(0, qrData.length() - 4) + "6304" + crc;
        
        log.info("QR code généré: longueur={}, CRC={}", result.length(), crc);
        log.debug("QR code complet: {}", result);
        
        return result;
    }
    
    /**
     * Valide les données avant génération
     * 
     * @param data Les données de paiement
     * @throws IllegalArgumentException si les données sont invalides
     */
    protected void validateData(QRPaymentData data) {
        if (data == null) {
            throw new IllegalArgumentException("Les données de paiement sont obligatoires");
        }
        
        if (data.getMerchantInfo() == null && data.getDynamicUrl() == null) {
            throw new IllegalArgumentException("Les informations du marchand ou une URL dynamique sont obligatoires");
        }
    }
    
    /**
     * Génère le QR code pour les données fournies
     * 
     * @param data Les données de paiement
     * @return La chaîne QR code EMVCo
     */
    public abstract String generate(QRPaymentData data);
}
