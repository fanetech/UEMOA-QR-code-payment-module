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
 * Générateur de QR codes pour transferts entre particuliers (P2P)
 */
@Slf4j
@Component
public class P2PQRGenerator extends BaseQRGenerator {
    
    public P2PQRGenerator(EMVFormatter formatter, 
                         CRCCalculator crcCalculator,
                         UemoaQrProperties properties) {
        super(formatter, crcCalculator, properties);
    }
    
    @Override
    public String generate(QRPaymentData data) {
        log.info("Génération d'un QR code P2P");
        
        // Validation
        validateData(data);
        if (data.getMerchantInfo() == null) {
            throw new IllegalArgumentException("Les informations du bénéficiaire sont obligatoires pour un QR P2P");
        }
        
        // Initialisation
        initializeDefaults();
        
        // Point d'initiation statique (les QR P2P sont toujours statiques)
        fields.put("01", "11");
        
        // Informations du compte du particulier
        setMerchantAccountInfo(data.getMerchantInfo().getAlias());
        
        // Détails du particulier
        fields.put("58", data.getMerchantInfo().getCountryCode());
        
        // Pour P2P, le nom peut être masqué pour la confidentialité
        String name = data.getMerchantInfo().getName();
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("XXX")) {
            name = "XXX"; // Nom masqué par défaut
        }
        fields.put("59", name);
        
        fields.put("60", data.getMerchantInfo().getCity());
        
        // Pour P2P, le montant est généralement saisi par l'envoyeur
        if (data.getAmount() != null) {
            fields.put("54", data.getAmount().toPlainString());
            log.debug("Montant prédéfini: {} XOF", data.getAmount());
        }
        
        // Données additionnelles
        Map<String, String> additionalData = new HashMap<>();
        
        // Canal P2P statique (731)
        additionalData.put("11", String.valueOf(
            QRPaymentData.MerchantChannel.P2P_STATIC.getCode()));
        log.debug("Canal P2P: {}", QRPaymentData.MerchantChannel.P2P_STATIC.getCode());
        
        // Message ou référence optionnelle
        if (data.getTransactionId() != null && !data.getTransactionId().isEmpty()) {
            additionalData.put("01", data.getTransactionId());
            log.debug("Référence P2P: {}", data.getTransactionId());
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
        
        log.info("QR code P2P généré avec succès pour: {}", 
                data.getMerchantInfo().getAlias());
        
        return qrCode;
    }
}
