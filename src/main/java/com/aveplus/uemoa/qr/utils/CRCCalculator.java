package com.aveplus.uemoa.qr.utils;

import org.springframework.stereotype.Component;

/**
 * Calculateur de CRC16-CCITT pour la validation des QR codes EMVCo
 */
@Component
public class CRCCalculator {
    
    private static final int POLYNOMIAL = 0x1021;
    private static final int INITIAL_VALUE = 0xFFFF;
    
    /**
     * Calcule le CRC16-CCITT d'une chaîne de caractères
     * 
     * @param data Les données pour lesquelles calculer le CRC
     * @return Le CRC au format hexadécimal (4 caractères)
     */
    public String calculate(String data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Les données ne peuvent pas être nulles ou vides");
        }
        
        int crc = INITIAL_VALUE;
        byte[] bytes = data.getBytes();
        
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ POLYNOMIAL;
                } else {
                    crc = crc << 1;
                }
            }
        }
        
        return String.format("%04X", crc & 0xFFFF);
    }
    
    /**
     * Vérifie si le CRC d'un QR code est valide
     * 
     * @param qrData Les données complètes du QR code incluant le CRC
     * @return true si le CRC est valide, false sinon
     */
    public boolean validate(String qrData) {
        if (qrData == null || qrData.length() < 8) {
            return false;
        }
        
        // Le CRC est les 4 derniers caractères
        String providedCrc = qrData.substring(qrData.length() - 4);
        
        // Calcule le CRC sur les données sans le CRC final
        String dataWithoutCrc = qrData.substring(0, qrData.length() - 4);
        String calculatedCrc = calculate(dataWithoutCrc);
        
        return providedCrc.equalsIgnoreCase(calculatedCrc);
    }
}
