package com.aveplus.uemoa.qr;

import com.aveplus.uemoa.qr.config.UemoaQrProperties;
import com.aveplus.uemoa.qr.generator.StaticQRGenerator;
import com.aveplus.uemoa.qr.model.MerchantInfo;
import com.aveplus.uemoa.qr.model.QRPaymentData;
import com.aveplus.uemoa.qr.utils.CRCCalculator;
import com.aveplus.uemoa.qr.utils.EMVFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires simples SANS Spring
 * Ces tests fonctionnent sans contexte Spring
 */
public class UemoaQRServiceTest {
    
    private StaticQRGenerator staticQRGenerator;
    private EMVFormatter formatter;
    private CRCCalculator crcCalculator;
    private UemoaQrProperties properties;
    
    @BeforeEach
    public void setUp() {
        // Initialisation manuelle sans Spring
        formatter = new EMVFormatter();
        crcCalculator = new CRCCalculator();
        properties = new UemoaQrProperties();
        properties.setDefaultCountryCode("CI");
        properties.setCurrencyCode("952");
        properties.setPayloadFormatIndicator("01");
        properties.setMerchantCategoryCode("0000");
        properties.setBceaoPrefix("int.bceao.pi");
        
        staticQRGenerator = new StaticQRGenerator(formatter, crcCalculator, properties);
    }
    
    @Test
    public void testGenerateStaticQR() {
        // Préparation
        MerchantInfo merchantInfo = MerchantInfo.builder()
            .alias("test-123")
            .name("TEST SHOP")
            .city("Abidjan")
            .countryCode("CI")
            .build();
        
        QRPaymentData data = QRPaymentData.builder()
            .type(QRPaymentData.QRType.STATIC)
            .merchantInfo(merchantInfo)
            .build();
        
        // Génération
        String qrCode = staticQRGenerator.generate(data);
        
        // Vérifications
        assertNotNull(qrCode);
        assertTrue(qrCode.length() > 50);
        assertTrue(qrCode.contains("5303952")); // Code devise XOF
        assertTrue(qrCode.contains("5802CI")); // Code pays
        
        System.out.println("QR généré: " + qrCode);
    }
    
    @Test
    public void testGenerateStaticQRWithAmount() {
        // Préparation
        MerchantInfo merchantInfo = MerchantInfo.builder()
            .alias("test-456")
            .name("RESTO TEST")
            .city("Yamoussoukro")
            .countryCode("CI")
            .build();
        
        QRPaymentData data = QRPaymentData.builder()
            .type(QRPaymentData.QRType.STATIC)
            .merchantInfo(merchantInfo)
            .amount(new BigDecimal("5000"))
            .build();
        
        // Génération
        String qrCode = staticQRGenerator.generate(data);
        
        // Vérifications
        assertNotNull(qrCode);
        assertTrue(qrCode.contains("54045000")); // Montant
        
        System.out.println("QR avec montant: " + qrCode);
    }
    
    @Test
    public void testCRCCalculation() {
        String data = "00020101021238" + "6304";
        String crc = crcCalculator.calculate(data);
        
        assertNotNull(crc);
        assertEquals(4, crc.length());
        
        System.out.println("CRC calculé: " + crc);
    }
    
    @Test
    public void testEMVFormatting() {
        String formatted = formatter.formatField("00", "01");
        assertEquals("000201", formatted);
        
        formatted = formatter.formatField("52", "0000");
        assertEquals("52040000", formatted); // ID(52) + Length(04) + Value(0000)
        
        formatted = formatter.formatField("53", "952");
        assertEquals("5303952", formatted); // ID(53) + Length(03) + Value(952)
    }
}
