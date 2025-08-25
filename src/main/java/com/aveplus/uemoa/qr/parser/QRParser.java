package com.aveplus.uemoa.qr.parser;

import com.aveplus.uemoa.qr.model.EMVField;
import com.aveplus.uemoa.qr.model.MerchantInfo;
import com.aveplus.uemoa.qr.model.QRPaymentData;
import com.aveplus.uemoa.qr.utils.CRCCalculator;
import com.aveplus.uemoa.qr.utils.EMVFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser pour lire et interpréter les QR codes EMVCo UEMOA
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QRParser {
    
    private final EMVFormatter formatter;
    private final CRCCalculator crcCalculator;
    
    /**
     * Parse un QR code EMVCo et retourne les données de paiement
     * 
     * @param qrData La chaîne de données du QR code
     * @return Les données de paiement extraites
     * @throws IllegalArgumentException si le QR code est invalide
     */
    public QRPaymentData parse(String qrData) {
        log.debug("Parsing QR code: {}", qrData);
        
        if (qrData == null || qrData.isEmpty()) {
            throw new IllegalArgumentException("Les données du QR code ne peuvent pas être vides");
        }
        
        // Vérifie le CRC si présent
        if (qrData.length() > 8) {
            validateCRC(qrData);
        }
        
        // Parse tous les champs EMV
        List<EMVField> fields = formatter.parseAllFields(qrData);
        Map<String, String> fieldsMap = formatter.fieldsToMap(fields);
        
        // Crée l'objet de données de paiement
        QRPaymentData paymentData = new QRPaymentData();
        
        // Détermine le type de QR
        String initiationMethod = fieldsMap.get("01");
        if ("12".equals(initiationMethod)) {
            paymentData.setType(QRPaymentData.QRType.DYNAMIC);
        } else {
            paymentData.setType(QRPaymentData.QRType.STATIC);
        }
        
        // Parse les informations du marchand/particulier
        parseMerchantInfo(fieldsMap, paymentData);
        
        // Parse le montant
        String amount = fieldsMap.get("54");
        if (amount != null && !amount.isEmpty()) {
            try {
                paymentData.setAmount(new BigDecimal(amount));
                log.debug("Montant parsé: {} XOF", amount);
            } catch (NumberFormatException e) {
                log.warn("Montant invalide: {}", amount);
            }
        }
        
        // Parse les données additionnelles
        parseAdditionalData(fieldsMap, paymentData);
        
        log.info("QR code parsé avec succès: type={}, montant={}", 
                paymentData.getType(), paymentData.getAmount());
        
        return paymentData;
    }
    
    /**
     * Valide le CRC du QR code
     * 
     * @param qrData Les données du QR code
     * @throws IllegalArgumentException si le CRC est invalide
     */
    private void validateCRC(String qrData) {
        // Recherche le champ CRC (63)
        int crcFieldIndex = qrData.lastIndexOf("6304");
        if (crcFieldIndex > 0 && crcFieldIndex == qrData.length() - 8) {
            boolean isValid = crcCalculator.validate(qrData);
            if (!isValid) {
                log.error("CRC invalide pour le QR code");
                throw new IllegalArgumentException("Le QR code a un CRC invalide");
            }
            log.debug("CRC validé avec succès");
        }
    }
    
    /**
     * Parse les informations du marchand ou du particulier
     */
    private void parseMerchantInfo(Map<String, String> fields, QRPaymentData paymentData) {
        // Parse le champ 36 (Merchant Account Information)
        String merchantAccountInfo = fields.get("36");
        if (merchantAccountInfo != null) {
            List<EMVField> subFields = formatter.parseSubFields(merchantAccountInfo);
            Map<String, String> subFieldsMap = formatter.fieldsToMap(subFields);
            
            String alias = subFieldsMap.get("01"); // Alias standard
            if (alias == null || alias.isEmpty()) {
                alias = subFieldsMap.get("02"); // URL ou alias alternatif
                if (alias != null && alias.startsWith("pi.")) {
                    // C'est une URL dynamique
                    paymentData.setDynamicUrl(alias);
                }
            }
            
            if (alias != null && !alias.isEmpty()) {
                MerchantInfo merchantInfo = MerchantInfo.builder()
                    .alias(alias)
                    .name(fields.get("59"))
                    .city(fields.get("60"))
                    .countryCode(fields.get("58"))
                    .categoryCode(fields.get("52"))
                    .build();
                
                paymentData.setMerchantInfo(merchantInfo);
                log.debug("Merchant info parsé: {}", merchantInfo.getName());
            }
        }
    }
    
    /**
     * Parse les données additionnelles
     */
    private void parseAdditionalData(Map<String, String> fields, QRPaymentData paymentData) {
        String additionalData = fields.get("62");
        if (additionalData == null || additionalData.isEmpty()) {
            return;
        }
        
        List<EMVField> subFields = formatter.parseSubFields(additionalData);
        Map<String, String> subFieldsMap = formatter.fieldsToMap(subFields);
        
        // Transaction ID
        String txId = subFieldsMap.get("01");
        if (txId != null) {
            paymentData.setTransactionId(txId);
            log.debug("Transaction ID parsé: {}", txId);
        }
        
        // Référence de facture
        String billRef = subFieldsMap.get("02");
        if (billRef != null) {
            paymentData.setBillReference(billRef);
            log.debug("Référence facture parsée: {}", billRef);
        }
        
        // ID d'abonnement
        String subscriptionId = subFieldsMap.get("03");
        if (subscriptionId != null) {
            paymentData.setSubscriptionId(subscriptionId);
            log.debug("ID abonnement parsé: {}", subscriptionId);
        }
        
        // Canal marchand
        String merchantChannel = subFieldsMap.get("11");
        if (merchantChannel != null) {
            try {
                int channelCode = Integer.parseInt(merchantChannel);
                QRPaymentData.MerchantChannel channel = 
                    QRPaymentData.MerchantChannel.fromCode(channelCode);
                
                if (channel != null) {
                    paymentData.setMerchantChannel(channel);
                    
                    // Détermine si c'est un P2P basé sur le canal
                    if (channel == QRPaymentData.MerchantChannel.P2P_STATIC) {
                        paymentData.setType(QRPaymentData.QRType.P2P);
                    }
                    
                    log.debug("Canal marchand parsé: {} ({})", 
                             channel.getCode(), channel.getDescription());
                }
            } catch (NumberFormatException e) {
                log.warn("Canal marchand invalide: {}", merchantChannel);
            }
        }
        
        // Stocke toutes les autres données additionnelles
        Map<String, String> additionalDataMap = new HashMap<>(subFieldsMap);
        // Retire les champs déjà traités
        additionalDataMap.remove("01");
        additionalDataMap.remove("02");
        additionalDataMap.remove("03");
        additionalDataMap.remove("11");
        
        if (!additionalDataMap.isEmpty()) {
            paymentData.setAdditionalData(additionalDataMap);
        }
    }
}
