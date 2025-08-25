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
 * Générateur de QR codes statiques pour marchands
 */
@Slf4j
@Component
public class StaticQRGenerator extends BaseQRGenerator {
    
    public StaticQRGenerator(EMVFormatter formatter, 
                            CRCCalculator crcCalculator,
                            UemoaQrProperties properties) {
        super(formatter, crcCalculator, properties);
    }
    
    @Override
    public String generate(QRPaymentData data) {
        log.info("Génération d'un QR code statique");
        
        // Validation
        validateData(data);
        if (data.getMerchantInfo() == null) {
            throw new IllegalArgumentException("Les informations du marchand sont obligatoires pour un QR statique");
        }
        
        // Initialisation
        initializeDefaults();
        
        // Point d'initiation statique
        fields.put("01", "11");
        
        // Informations du compte marchand
        setMerchantAccountInfo(data.getMerchantInfo().getAlias());
        
        // Détails du marchand
        fields.put("58", data.getMerchantInfo().getCountryCode());
        fields.put("59", data.getMerchantInfo().getName());
        fields.put("60", data.getMerchantInfo().getCity());
        
        // Montant (optionnel pour QR statique)
        if (data.getAmount() != null) {
            fields.put("54", data.getAmount().toPlainString());
            log.debug("Montant configuré: {} XOF", data.getAmount());
        }
        
        // Données additionnelles
        Map<String, String> additionalData = new HashMap<>();
        
        // ID de transaction
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
        
        // Canal marchand
        if (data.getMerchantChannel() != null) {
            additionalData.put("11", String.valueOf(data.getMerchantChannel().getCode()));
            log.debug("Canal marchand: {} ({})", 
                     data.getMerchantChannel().getCode(), 
                     data.getMerchantChannel().getDescription());
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
        
        log.info("QR code statique généré avec succès pour: {}", 
                data.getMerchantInfo().getName());
        
        return qrCode;
    }
}
