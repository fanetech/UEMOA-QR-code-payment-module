package com.aveplus.uemoa.qr.service;

import com.aveplus.uemoa.qr.config.UemoaQrProperties;
import com.aveplus.uemoa.qr.generator.DynamicQRGenerator;
import com.aveplus.uemoa.qr.generator.P2PQRGenerator;
import com.aveplus.uemoa.qr.generator.StaticQRGenerator;
import com.aveplus.uemoa.qr.model.QRPaymentData;
import com.aveplus.uemoa.qr.parser.QRParser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service principal pour la gestion des QR codes de paiement UEMOA
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UemoaQRService {
    
    private final StaticQRGenerator staticQRGenerator;
    private final DynamicQRGenerator dynamicQRGenerator;
    private final P2PQRGenerator p2pQRGenerator;
    private final QRParser qrParser;
    private final UemoaQrProperties properties;
    
    /**
     * Génère les données du QR code selon le type spécifié
     * 
     * @param data Les données de paiement
     * @return La chaîne de données EMVCo du QR code
     */
    public String generateQRData(@Valid QRPaymentData data) {
        if (data == null) {
            throw new IllegalArgumentException("Les données de paiement sont obligatoires");
        }
        
        log.info("Génération de QR code: type={}, marchand={}", 
                data.getType(), 
                data.getMerchantInfo() != null ? data.getMerchantInfo().getName() : "N/A");
        
        String qrData;
        switch (data.getType()) {
            case STATIC:
                qrData = staticQRGenerator.generate(data);
                break;
            case DYNAMIC:
                qrData = dynamicQRGenerator.generate(data);
                break;
            case P2P:
                qrData = p2pQRGenerator.generate(data);
                break;
            default:
                throw new IllegalArgumentException("Type de QR code non supporté: " + data.getType());
        }
        
        log.debug("QR code généré: {}", qrData);
        return qrData;
    }
    
    /**
     * Génère une image QR code en Base64
     * 
     * @param data Les données de paiement
     * @return L'image QR code encodée en Base64
     * @throws WriterException En cas d'erreur de génération
     * @throws IOException En cas d'erreur d'écriture
     */
    public String generateQRImage(@Valid QRPaymentData data) throws WriterException, IOException {
        String qrData = generateQRData(data);
        return generateQRImageFromString(qrData);
    }
    
    /**
     * Génère une image QR code à partir d'une chaîne de données
     * 
     * @param qrData La chaîne de données EMVCo
     * @return L'image QR code encodée en Base64
     * @throws WriterException En cas d'erreur de génération
     * @throws IOException En cas d'erreur d'écriture
     */
    public String generateQRImageFromString(String qrData) throws WriterException, IOException {
        if (!properties.isGenerateQrImage()) {
            throw new UnsupportedOperationException("La génération d'images QR est désactivée");
        }
        
        byte[] imageBytes = generateQRImageBytes(qrData);
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * Génère une image QR code sous forme de tableau d'octets
     * 
     * @param data Les données de paiement
     * @return L'image QR code en bytes
     * @throws WriterException En cas d'erreur de génération
     * @throws IOException En cas d'erreur d'écriture
     */
    public byte[] generateQRImageBytes(@Valid QRPaymentData data) throws WriterException, IOException {
        String qrData = generateQRData(data);
        return generateQRImageBytes(qrData);
    }
    
    /**
     * Génère une image QR code sous forme de tableau d'octets à partir d'une chaîne
     * 
     * @param qrData La chaîne de données EMVCo
     * @return L'image QR code en bytes
     * @throws WriterException En cas d'erreur de génération
     * @throws IOException En cas d'erreur d'écriture
     */
    public byte[] generateQRImageBytes(String qrData) throws WriterException, IOException {
        if (!properties.isGenerateQrImage()) {
            throw new UnsupportedOperationException("La génération d'images QR est désactivée");
        }
        
        log.debug("Génération d'image QR: taille={}px, marge={}px", 
                properties.getQrImageSize(), properties.getQrImageMargin());
        
        // Configuration des hints pour la génération
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, properties.getQrImageMargin());
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        
        // Génération du QR code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
            qrData, 
            BarcodeFormat.QR_CODE, 
            properties.getQrImageSize(), 
            properties.getQrImageSize(),
            hints
        );
        
        // Conversion en image
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, properties.getImageFormat(), outputStream);
        
        byte[] imageBytes = outputStream.toByteArray();
        log.info("Image QR générée: {} bytes, format={}", imageBytes.length, properties.getImageFormat());
        
        return imageBytes;
    }
    
    /**
     * Parse un QR code EMVCo et retourne les données de paiement
     * 
     * @param qrData La chaîne de données du QR code
     * @return Les données de paiement extraites
     */
    public QRPaymentData parseQRCode(String qrData) {
        log.info("Parsing du QR code");
        return qrParser.parse(qrData);
    }
    
    /**
     * Génère un QR code statique pour marchand
     * 
     * @param data Les données de paiement
     * @return La chaîne de données EMVCo
     */
    public String generateStaticQR(@Valid QRPaymentData data) {
        data.setType(QRPaymentData.QRType.STATIC);
        return generateQRData(data);
    }
    
    /**
     * Génère un QR code dynamique
     * 
     * @param data Les données de paiement
     * @return La chaîne de données EMVCo
     */
    public String generateDynamicQR(@Valid QRPaymentData data) {
        data.setType(QRPaymentData.QRType.DYNAMIC);
        return generateQRData(data);
    }
    
    /**
     * Génère un QR code P2P
     * 
     * @param data Les données de paiement
     * @return La chaîne de données EMVCo
     */
    public String generateP2PQR(@Valid QRPaymentData data) {
        data.setType(QRPaymentData.QRType.P2P);
        return generateQRData(data);
    }
    
    /**
     * Valide un QR code EMVCo
     * 
     * @param qrData La chaîne de données du QR code
     * @return true si le QR code est valide
     */
    public boolean validateQRCode(String qrData) {
        try {
            QRPaymentData parsed = parseQRCode(qrData);
            return parsed != null;
        } catch (Exception e) {
            log.error("QR code invalide: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtient des informations détaillées sur un QR code
     * 
     * @param qrData La chaîne de données du QR code
     * @return Un map contenant les détails du QR code
     */
    public Map<String, Object> getQRCodeDetails(String qrData) {
        Map<String, Object> details = new HashMap<>();
        
        try {
            QRPaymentData parsed = parseQRCode(qrData);
            
            details.put("valid", true);
            details.put("type", parsed.getType());
            
            if (parsed.getMerchantInfo() != null) {
                Map<String, String> merchantDetails = new HashMap<>();
                merchantDetails.put("name", parsed.getMerchantInfo().getName());
                merchantDetails.put("city", parsed.getMerchantInfo().getCity());
                merchantDetails.put("country", parsed.getMerchantInfo().getCountryCode());
                merchantDetails.put("alias", parsed.getMerchantInfo().getAlias());
                details.put("merchant", merchantDetails);
            }
            
            if (parsed.getAmount() != null) {
                details.put("amount", parsed.getAmount());
            }
            
            if (parsed.getTransactionId() != null) {
                details.put("transactionId", parsed.getTransactionId());
            }
            
            if (parsed.getMerchantChannel() != null) {
                Map<String, Object> channelDetails = new HashMap<>();
                channelDetails.put("code", parsed.getMerchantChannel().getCode());
                channelDetails.put("description", parsed.getMerchantChannel().getDescription());
                details.put("channel", channelDetails);
            }
            
        } catch (Exception e) {
            details.put("valid", false);
            details.put("error", e.getMessage());
        }
        
        return details;
    }
}
