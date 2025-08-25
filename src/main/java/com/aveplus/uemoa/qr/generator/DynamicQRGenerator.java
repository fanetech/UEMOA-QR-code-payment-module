package com.aveplus.uemoa.qr.generator;

import com.aveplus.uemoa.qr.config.UemoaQrProperties;
import com.aveplus.uemoa.qr.model.QRPaymentData;
import com.aveplus.uemoa.qr.utils.CRCCalculator;
import com.aveplus.uemoa.qr.utils.EMVFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Générateur de QR codes dynamiques pour transactions
 */
@Slf4j
@Component
public class DynamicQRGenerator extends BaseQRGenerator {
    
    public DynamicQRGenerator(EMVFormatter formatter, 
                              CRCCalculator crcCalculator,
                              UemoaQrProperties properties) {
        super(formatter, crcCalculator, properties);
    }
    
    @Override
    public String generate(QRPaymentData data) {
        log.info("Génération d'un QR code dynamique");
        
        // Validation
        validateData(data);
        
        // Pour un QR dynamique, on doit avoir soit une URL, soit les infos marchands
        if (data.getDynamicUrl() == null && data.getMerchantInfo() == null) {
            throw new IllegalArgumentException(
                "Une URL dynamique ou les informations du marchand sont obligatoires pour un QR dynamique");
        }
        
        // Le montant est généralement obligatoire pour un QR dynamique
        if (data.getAmount() == null) {
            log.warn("Aucun montant spécifié pour le QR dynamique");
        }
        
        // Initialisation
        initializeDefaults();
        
        // Point d'initiation dynamique
        fields.put("01", "12");
        
        // Configuration du compte marchand
        if (data.getDynamicUrl() != null && !data.getDynamicUrl().isEmpty()) {
            // Utilise l'URL dynamique
            setDynamicMerchantInfo(data.getDynamicUrl());
        } else {
            // Utilise l'alias marchand
            setMerchantAccountInfo(data.getMerchantInfo().getAlias());
        }
        
        // Détails du marchand (si fournis)
        if (data.getMerchantInfo() != null) {
            fields.put("58", data.getMerchantInfo().getCountryCode());
            fields.put("59", data.getMerchantInfo().getName());
            fields.put("60", data.getMerchantInfo().getCity());
        }
        
        // Montant (fortement recommandé pour QR dynamique)
        if (data.getAmount() != null) {
            fields.put("54", data.getAmount().toPlainString());
            log.debug("Montant configuré: {} XOF", data.getAmount());
        }
        
        // Données additionnelles
        Map<String, String> additionalData = new HashMap<>();
        
        // ID de transaction (fortement recommandé pour traçabilité)
        if (data.getTransactionId() != null && !data.getTransactionId().isEmpty()) {
            additionalData.put("01", data.getTransactionId());
            log.debug("Transaction ID: {}", data.getTransactionId());
        }
        
        // Référence de facture
        if (data.getBillReference() != null && !data.getBillReference().isEmpty()) {
            additionalData.put("02", data.getBillReference());
            log.debug("Référence facture: {}", data.getBillReference());
        }
        
        // ID d'abonnement
        if (data.getSubscriptionId() != null && !data.getSubscriptionId().isEmpty()) {
            additionalData.put("03", data.getSubscriptionId());
            log.debug("ID abonnement: {}", data.getSubscriptionId());
        }
        
        // Canal marchand (important pour identifier le contexte)
        if (data.getMerchantChannel() != null) {
            additionalData.put("11", String.valueOf(data.getMerchantChannel().getCode()));
            log.debug("Canal marchand: {} ({})", 
                     data.getMerchantChannel().getCode(), 
                     data.getMerchantChannel().getDescription());
        } else {
            // Par défaut, QR dynamique sur site
            additionalData.put("11", String.valueOf(
                QRPaymentData.MerchantChannel.DYNAMIC_ONSITE.getCode()));
        }
        
        // Ajoute les données additionnelles personnalisées
        if (data.getAdditionalData() != null) {
            data.getAdditionalData().forEach((key, value) -> {
                if (!additionalData.containsKey(key)) {
                    additionalData.put(key, value);
                }
            });
        }
        
        setAdditionalData(additionalData);
        
        // Construction du QR code final
        String qrCode = buildQRString();
        
        log.info("QR code dynamique généré avec succès" + 
                (data.getTransactionId() != null ? " pour transaction: " + data.getTransactionId() : ""));
        
        return qrCode;
    }
}
