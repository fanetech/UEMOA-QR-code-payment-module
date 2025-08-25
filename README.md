# Module UEMOA QR Code Payment

Module Spring Boot pour la génération et le parsing de QR codes de paiement instantané dans l'espace UEMOA, conformément aux normes EMVCo et aux spécifications de la BCEAO.

## 🚀 Fonctionnalités

- ✅ **Génération de QR codes statiques** pour marchands (avec ou sans montant)
- ✅ **Génération de QR codes dynamiques** pour transactions spécifiques
- ✅ **Génération de QR codes P2P** pour transferts entre particuliers
- ✅ **Parsing et validation** de QR codes EMVCo
- ✅ **Génération d'images QR** (PNG, JPG)
- ✅ **Support complet des pays UEMOA** (BF, CI, TG, SN, ML, BJ, GW, NE)
- ✅ **Validation CRC16-CCITT**
- ✅ **Configuration flexible** via application.properties

## 📦 Installation

### 1. Compiler et installer le module localement

```bash
cd /Users/pro/Desktop/projects/aveplus/uemoa-qrcode-module
mvn clean install
```

### 2. Ajouter la dépendance dans votre projet

```xml
<dependency>
    <groupId>com.aveplus</groupId>
    <artifactId>uemoa-qrcode-module</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🔧 Configuration

Ajoutez ces propriétés dans votre `application.yml` ou `application.properties` :

```yaml
uemoa:
  qr:
    payment:
      default-country-code: CI        # Code pays par défaut
      currency-code: 952              # XOF (Franc CFA)
      validate-crc: true              # Validation du checksum
      generate-qr-image: true         # Génération d'images
      qr-image-size: 300              # Taille en pixels
      image-format: PNG               # Format (PNG ou JPG)
      debug-mode: false               # Logs détaillés
```

## 💻 Utilisation

### Import du service

```java
import com.aveplus.uemoa.qr.service.UemoaQRService;
import com.aveplus.uemoa.qr.model.*;

@Service
public class PaymentService {
    
    @Autowired
    private UemoaQRService qrService;
    
    // Votre code...
}
```

### 1. QR Code Statique (Marchand)

#### Sans montant (le client saisit le montant)

```java
MerchantInfo merchantInfo = MerchantInfo.builder()
    .alias("111c3e1b-4312-49ec-b75e-4c8c74c10fd7")
    .name("BOUTIQUE EXEMPLE")
    .city("Yamoussoukro")
    .countryCode("CI")
    .build();

QRPaymentData data = QRPaymentData.builder()
    .type(QRPaymentData.QRType.STATIC)
    .merchantInfo(merchantInfo)
    .merchantChannel(MerchantChannel.STATIC_ONSITE)
    .build();

// Génération du QR code
String qrCode = qrService.generateQRData(data);

// Génération de l'image QR en Base64
String qrImageBase64 = qrService.generateQRImage(data);

// Génération de l'image en bytes
byte[] qrImageBytes = qrService.generateQRImageBytes(data);
```

#### Avec montant fixe

```java
QRPaymentData data = QRPaymentData.builder()
    .type(QRPaymentData.QRType.STATIC)
    .merchantInfo(merchantInfo)
    .amount(new BigDecimal("15000"))  // 15 000 XOF
    .merchantChannel(MerchantChannel.STATIC_WITH_AMOUNT)
    .build();

String qrCode = qrService.generateQRData(data);
```

#### QR sur facture

```java
QRPaymentData data = QRPaymentData.builder()
    .type(QRPaymentData.QRType.STATIC)
    .merchantInfo(merchantInfo)
    .amount(new BigDecimal("17000"))
    .billReference("A7550775299Y")
    .subscriptionId("0210305-A251X")
    .merchantChannel(MerchantChannel.STATIC_INVOICE)
    .build();

String qrCode = qrService.generateQRData(data);
```

### 2. QR Code Dynamique

#### Pour paiement sur site

```java
QRPaymentData data = QRPaymentData.builder()
    .type(QRPaymentData.QRType.DYNAMIC)
    .merchantInfo(merchantInfo)
    .amount(new BigDecimal("18000"))
    .transactionId("TXN-2024-001")
    .merchantChannel(MerchantChannel.DYNAMIC_ONSITE)
    .build();

String qrCode = qrService.generateQRData(data);
```

#### Pour e-commerce

```java
QRPaymentData data = QRPaymentData.builder()
    .type(QRPaymentData.QRType.DYNAMIC)
    .merchantInfo(merchantInfo)
    .amount(new BigDecimal("22500"))
    .transactionId("ORDER-12345")
    .dynamicUrl("pi.psp.ci/jgjh86lkzdhfqgatehjnx")
    .merchantChannel(MerchantChannel.DYNAMIC_ECOMMERCE_WEB)
    .build();

String qrCode = qrService.generateQRData(data);
```

### 3. QR Code P2P (Transfert entre particuliers)

```java
MerchantInfo userInfo = MerchantInfo.builder()
    .alias("341c3e1b-4312-49gc-b75e-4i8c74c60hJ2")
    .name("Jean Dupont")  // Ou "XXX" pour masquer
    .city("Notsé")
    .countryCode("TG")
    .build();

QRPaymentData data = QRPaymentData.builder()
    .type(QRPaymentData.QRType.P2P)
    .merchantInfo(userInfo)
    .build();

String qrCode = qrService.generateQRData(data);
```

### 4. Parser un QR Code

```java
// Lecture d'un QR code scanné
String scannedQR = "00020136560012int.bceao.pi...";

// Parse les données
QRPaymentData parsed = qrService.parseQRCode(scannedQR);

// Accès aux informations
System.out.println("Type: " + parsed.getType());
System.out.println("Marchand: " + parsed.getMerchantInfo().getName());
System.out.println("Montant: " + parsed.getAmount());
System.out.println("Transaction ID: " + parsed.getTransactionId());

// Validation
boolean isValid = qrService.validateQRCode(scannedQR);

// Détails complets
Map<String, Object> details = qrService.getQRCodeDetails(scannedQR);
```

## 📊 Structure des QR Codes

### Champs EMV supportés

| ID | Nom | Obligatoire | Description |
|----|-----|-------------|-------------|
| 00 | Payload Format Indicator | ✅ | Toujours "01" |
| 01 | Point of Initiation Method | ❌ | "11" (statique), "12" (dynamique) |
| 36 | Merchant Account Info | ✅ | Contient l'alias du compte |
| 52 | Merchant Category Code | ✅ | "0000" pour tous |
| 53 | Currency Code | ✅ | "952" (XOF) |
| 54 | Amount | ❌ | Montant de la transaction |
| 58 | Country Code | ✅ | Code pays ISO |
| 59 | Merchant Name | ✅ | Nom du marchand |
| 60 | Merchant City | ✅ | Ville |
| 62 | Additional Data | ❌ | Données supplémentaires |
| 63 | CRC | ✅ | Checksum |

### Canaux marchands (Merchant Channel)

| Code | Type | Description |
|------|------|-------------|
| 100 | STATIC_ONSITE | QR statique sur site |
| 110 | STATIC_WITH_AMOUNT | QR statique avec montant |
| 120 | STATIC_WITH_TXID | QR statique avec ID transaction |
| 131 | STATIC_INVOICE | QR sur facture |
| 500 | DYNAMIC_ONSITE | QR dynamique sur site |
| 521 | DYNAMIC_ECOMMERCE_WEB | E-commerce Web |
| 522 | DYNAMIC_ECOMMERCE_APP | E-commerce App |
| 731 | P2P_STATIC | Transfert entre particuliers |

## 🌍 Codes pays UEMOA

- **BF** : Burkina Faso
- **CI** : Côte d'Ivoire
- **TG** : Togo
- **SN** : Sénégal
- **ML** : Mali
- **BJ** : Bénin
- **GW** : Guinée-Bissau
- **NE** : Niger

## 🔌 Intégration API REST

Si vous voulez exposer les fonctionnalités via REST :

```java
@RestController
@RequestMapping("/api/qr")
public class QRController {
    
    @Autowired
    private UemoaQRService qrService;
    
    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateQR(@RequestBody @Valid QRPaymentData data) {
        try {
            String qrData = qrService.generateQRData(data);
            String qrImage = qrService.generateQRImage(data);
            
            Map<String, String> response = new HashMap<>();
            response.put("data", qrData);
            response.put("image", qrImage);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/parse")
    public ResponseEntity<QRPaymentData> parseQR(@RequestBody Map<String, String> request) {
        String qrData = request.get("qrData");
        QRPaymentData parsed = qrService.parseQRCode(qrData);
        return ResponseEntity.ok(parsed);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateQR(@RequestBody Map<String, String> request) {
        String qrData = request.get("qrData");
        boolean isValid = qrService.validateQRCode(qrData);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        
        return ResponseEntity.ok(response);
    }
}
```

## 🧪 Tests

Exécutez les tests avec :

```bash
mvn test
```

## 📝 Exemples de QR Codes générés

### QR Statique sans montant
```
00020136560012int.bceao.pi0136111c3e1b-4312-49ec-b75e-4c8c74c10fd7520400005303952
5802CI5903XXX6012Yamoussoukro620711030006304D18E
```

### QR Dynamique avec montant
```
00020112365200...pi.psp.ci/jgjh86lkzdhfqgatehjnx520400005303952540522500
5802CI5909E-COMMERCE6006Abidjan62...6304XXXX
```

## 🤝 Support

Pour toute question ou problème :
- Email : support@aveplus.com
- Documentation BCEAO : [Spécifications techniques](https://www.bceao.int)

## 📄 Licence

Copyright © 2024 AvePlus. Tous droits réservés.

## 🔄 Changelog

### Version 1.0.0
- Génération de QR codes statiques, dynamiques et P2P
- Parsing et validation EMVCo
- Support complet des pays UEMOA
- Génération d'images QR
- Tests unitaires complets
