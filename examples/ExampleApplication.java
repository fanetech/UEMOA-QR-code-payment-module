package com.aveplus.uemoa.qr.examples;

import com.aveplus.uemoa.qr.model.MerchantInfo;
import com.aveplus.uemoa.qr.model.QRPaymentData;
import com.aveplus.uemoa.qr.service.UemoaQRService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

/**
 * Exemple d'utilisation du module UEMOA QR Code
 */
@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(UemoaQRService qrService) {
        return (args) -> {
            System.out.println("\n========================================");
            System.out.println("EXEMPLES D'UTILISATION DU MODULE UEMOA QR");
            System.out.println("========================================\n");

            // ==========================================
            // 1. QR STATIQUE SANS MONTANT
            // ==========================================
            System.out.println("1. QR STATIQUE SANS MONTANT");
            System.out.println("----------------------------");
            
            MerchantInfo boutique = MerchantInfo.builder()
                .alias("111c3e1b-4312-49ec-b75e-4c8c74c10fd7")
                .name("BOUTIQUE CENTRALE")
                .city("Yamoussoukro")
                .countryCode("CI")
                .build();

            QRPaymentData staticQR = QRPaymentData.builder()
                .type(QRPaymentData.QRType.STATIC)
                .merchantInfo(boutique)
                .merchantChannel(QRPaymentData.MerchantChannel.STATIC_ONSITE)
                .build();

            String staticCode = qrService.generateQRData(staticQR);
            System.out.println("QR Code généré: " + staticCode);
            System.out.println("Longueur: " + staticCode.length() + " caractères");
            
            // Parse le QR généré
            QRPaymentData parsed = qrService.parseQRCode(staticCode);
            System.out.println("Vérifiation - Marchand: " + parsed.getMerchantInfo().getName());
            System.out.println("Vérifiation - Ville: " + parsed.getMerchantInfo().getCity());
            System.out.println();

            // ==========================================
            // 2. QR STATIQUE AVEC MONTANT
            // ==========================================
            System.out.println("2. QR STATIQUE AVEC MONTANT");
            System.out.println("----------------------------");
            
            MerchantInfo restaurant = MerchantInfo.builder()
                .alias("rest-2024-abj-001")
                .name("RESTAURANT LE GOURMET")
                .city("Abidjan")
                .countryCode("CI")
                .build();

            QRPaymentData staticWithAmount = QRPaymentData.builder()
                .type(QRPaymentData.QRType.STATIC)
                .merchantInfo(restaurant)
                .amount(new BigDecimal("25000"))
                .merchantChannel(QRPaymentData.MerchantChannel.STATIC_WITH_AMOUNT)
                .build();

            String staticAmountCode = qrService.generateQRData(staticWithAmount);
            System.out.println("QR Code généré: " + staticAmountCode);
            System.out.println("Montant encodé: 25000 XOF");
            System.out.println();

            // ==========================================
            // 3. QR DYNAMIQUE E-COMMERCE
            // ==========================================
            System.out.println("3. QR DYNAMIQUE E-COMMERCE");
            System.out.println("---------------------------");
            
            MerchantInfo ecommerce = MerchantInfo.builder()
                .alias("shop-online-tg-001")
                .name("SHOP EN LIGNE")
                .city("Lomé")
                .countryCode("TG")
                .build();

            QRPaymentData dynamicQR = QRPaymentData.builder()
                .type(QRPaymentData.QRType.DYNAMIC)
                .merchantInfo(ecommerce)
                .amount(new BigDecimal("45500"))
                .transactionId("CMD-2024-12345")
                .merchantChannel(QRPaymentData.MerchantChannel.DYNAMIC_ECOMMERCE_WEB)
                .build();

            String dynamicCode = qrService.generateQRData(dynamicQR);
            System.out.println("QR Code généré: " + dynamicCode);
            System.out.println("Transaction ID: CMD-2024-12345");
            System.out.println("Montant: 45500 XOF");
            System.out.println();

            // ==========================================
            // 4. QR FACTURE
            // ==========================================
            System.out.println("4. QR FACTURE");
            System.out.println("-------------");
            
            MerchantInfo utility = MerchantInfo.builder()
                .alias("cie-facturation")
                .name("CIE")
                .city("Abidjan")
                .countryCode("CI")
                .build();

            QRPaymentData invoiceQR = QRPaymentData.builder()
                .type(QRPaymentData.QRType.STATIC)
                .merchantInfo(utility)
                .amount(new BigDecimal("17850"))
                .billReference("FAC-2024-98765")
                .subscriptionId("ABO-CI-123456")
                .merchantChannel(QRPaymentData.MerchantChannel.STATIC_INVOICE)
                .build();

            String invoiceCode = qrService.generateQRData(invoiceQR);
            System.out.println("QR Code généré: " + invoiceCode);
            System.out.println("Référence facture: FAC-2024-98765");
            System.out.println("N° Abonnement: ABO-CI-123456");
            System.out.println("Montant: 17850 XOF");
            System.out.println();

            // ==========================================
            // 5. QR P2P (TRANSFERT ENTRE PARTICULIERS)
            // ==========================================
            System.out.println("5. QR P2P (TRANSFERT ENTRE PARTICULIERS)");
            System.out.println("-----------------------------------------");
            
            MerchantInfo particulier = MerchantInfo.builder()
                .alias("usr-225-0707123456")
                .name("Jean KOUASSI")
                .city("Bouaké")
                .countryCode("CI")
                .build();

            QRPaymentData p2pQR = QRPaymentData.builder()
                .type(QRPaymentData.QRType.P2P)
                .merchantInfo(particulier)
                .build();

            String p2pCode = qrService.generateQRData(p2pQR);
            System.out.println("QR Code généré: " + p2pCode);
            System.out.println("Bénéficiaire: Jean KOUASSI");
            System.out.println("Ville: Bouaké");
            System.out.println("Le montant sera saisi par l'émetteur");
            System.out.println();

            // ==========================================
            // 6. VALIDATION DE QR CODE
            // ==========================================
            System.out.println("6. VALIDATION DE QR CODE");
            System.out.println("------------------------");
            
            // QR valide
            boolean isValid1 = qrService.validateQRCode(staticCode);
            System.out.println("QR statique valide? " + isValid1);
            
            // QR invalide
            boolean isValid2 = qrService.validateQRCode("INVALID_QR_12345");
            System.out.println("QR invalide valide? " + isValid2);
            System.out.println();

            // ==========================================
            // 7. PARSING D'UN QR CODE EXISTANT
            // ==========================================
            System.out.println("7. PARSING D'UN QR CODE DE LA DOCUMENTATION");
            System.out.println("--------------------------------------------");
            
            String exampleQR = "00020136560012int.bceao.pi0136111c3e1b-4312-49ec-b75e-4c8c74c10fd7" +
                              "520400005303952" + "5802CI5903XXX6012Yamoussoukro620711030006304D18E";
            
            QRPaymentData exampleParsed = qrService.parseQRCode(exampleQR);
            System.out.println("Type: " + exampleParsed.getType());
            System.out.println("Pays: " + exampleParsed.getMerchantInfo().getCountryCode());
            System.out.println("Ville: " + exampleParsed.getMerchantInfo().getCity());
            System.out.println("Alias: " + exampleParsed.getMerchantInfo().getAlias());
            
            if (exampleParsed.getMerchantChannel() != null) {
                System.out.println("Canal: " + exampleParsed.getMerchantChannel().getDescription());
            }
            System.out.println();

            // ==========================================
            // 8. GÉNÉRATION D'IMAGE QR
            // ==========================================
            System.out.println("8. GÉNÉRATION D'IMAGE QR");
            System.out.println("------------------------");
            
            try {
                // Génère une image Base64
                String qrImageBase64 = qrService.generateQRImage(staticQR);
                System.out.println("Image QR générée en Base64");
                System.out.println("Longueur Base64: " + qrImageBase64.length() + " caractères");
                
                // Génère une image en bytes
                byte[] qrImageBytes = qrService.generateQRImageBytes(staticQR);
                System.out.println("Image QR générée en bytes");
                System.out.println("Taille: " + qrImageBytes.length + " bytes");
            } catch (Exception e) {
                System.err.println("Erreur lors de la génération de l'image: " + e.getMessage());
            }
            
            System.out.println("\n========================================");
            System.out.println("FIN DES EXEMPLES");
            System.out.println("========================================\n");
        };
    }
}
