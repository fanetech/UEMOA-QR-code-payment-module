package com.aveplus.uemoa.qr;

import com.aveplus.uemoa.qr.model.MerchantInfo;
import com.aveplus.uemoa.qr.model.QRPaymentData;
import com.aveplus.uemoa.qr.service.UemoaQRService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour le service UEMOA QR
 * 
 * Ces tests nécessitent un contexte Spring complet.
 * Ils sont désactivés par défaut car c'est un module (pas une application).
 * 
 * Pour les exécuter, retirez @Disabled ou lancez-les depuis l'application exemple.
 */
@Disabled("Tests d'intégration désactivés pour le module - À exécuter dans une application")
@SpringBootTest(classes = TestConfiguration.class)
public class UemoaQRServiceIntegrationTest {
    
    @Autowired
    private UemoaQRService qrService;
    
    @Test
    public void testGenerateStaticQR() {
        MerchantInfo merchantInfo = MerchantInfo.builder()
            .alias("111c3e1b-4312-49ec-b75e-4c8c74c10fd7")
            .name("BOUTIQUE TEST")
            .city("Yamoussoukro")
            .countryCode("CI")
            .build();
        
        QRPaymentData data = QRPaymentData.builder()
            .type(QRPaymentData.QRType.STATIC)
            .merchantInfo(merchantInfo)
            .merchantChannel(QRPaymentData.MerchantChannel.STATIC_ONSITE)
            .build();
        
        String qrCode = qrService.generateQRData(data);
        
        assertNotNull(qrCode);
        assertTrue(qrCode.startsWith("0002"));
        assertTrue(qrCode.contains("5303952"));
        assertTrue(qrCode.contains("5802CI"));
        assertTrue(qrCode.contains("6304"));
    }
}
