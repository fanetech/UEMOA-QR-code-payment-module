# 🎯 COMPRENDRE LA DIFFÉRENCE : MODULE vs APPLICATION

## ❓ Pourquoi pas de Main ni de Controller dans le module ?

### 📚 **LE MODULE (uemoa-qrcode-module)**
```
C'est une LIBRAIRIE = Une boîte à outils
├── ❌ PAS de main()
├── ❌ PAS de controller (sauf si vous voulez en fournir)
├── ✅ Services
├── ✅ Modèles
├── ✅ Utilitaires
└── ✅ Configuration
```

**Comme :**
- Lombok (pas de main)
- Jackson (pas de main)
- Apache Commons (pas de main)
- Spring Security (pas de main, juste des configurations)

### 🚀 **L'APPLICATION (example-application)**
```
C'est un PROGRAMME = Utilise la boîte à outils
├── ✅ A un main()
├── ✅ A des controllers
├── ✅ Importe votre module
├── ✅ Se lance sur un port
└── ✅ Expose une API REST
```

## 📊 Schéma d'architecture

```
┌─────────────────────────────────────────────┐
│         VOTRE APPLICATION FINALE            │
│                                             │
│  ┌──────────────────────────────────────┐  │
│  │         QrApiApplication.java        │  │ ← LE MAIN EST ICI !
│  │         (avec main())                │  │
│  └──────────────────────────────────────┘  │
│                     │                       │
│                     ▼                       │
│  ┌──────────────────────────────────────┐  │
│  │       QRCodeController.java          │  │ ← LES CONTROLLERS SONT ICI !
│  │    @RestController                   │  │
│  └──────────────────────────────────────┘  │
│                     │                       │
│                  utilise                    │
│                     │                       │
│  ┌──────────────────────────────────────┐  │
│  │    📦 uemoa-qrcode-module (JAR)     │  │ ← VOTRE MODULE (pas de main)
│  │    - UemoaQRService                  │  │
│  │    - QRPaymentData                   │  │
│  │    - Generators                      │  │
│  └──────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

## 🔧 Comment utiliser le module dans VOS projets

### Option 1 : Installer localement d'abord
```bash
# Dans le dossier du module
cd /Users/pro/Desktop/projects/aveplus/uemoa-qrcode-module
mvn clean install

# Puis dans VOTRE projet
cd /Users/pro/Desktop/projects/aveplus/example-application
mvn spring-boot:run
```

### Option 2 : Après publication sur Maven Central
```xml
<!-- Dans le pom.xml de VOTRE application -->
<dependency>
    <groupId>com.aveplus</groupId>
    <artifactId>uemoa-qrcode-module</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🚀 Lancer l'application exemple

```bash
# 1. D'abord installer le module
cd /Users/pro/Desktop/projects/aveplus/uemoa-qrcode-module
mvn clean install

# 2. Ensuite lancer l'application exemple
cd example-application
mvn spring-boot:run
```

**L'application sera disponible sur :**
- 🌐 API : http://localhost:8080
- 📚 Swagger : http://localhost:8080/swagger-ui.html
- 💚 Health : http://localhost:8080/actuator/health

## 📝 Tester l'API

### Avec curl :
```bash
# Générer un QR statique
curl -X POST http://localhost:8080/api/v1/qr/generate \
  -H "Content-Type: application/json" \
  -d '{
    "type": "STATIC",
    "merchantInfo": {
      "alias": "test-123",
      "name": "BOUTIQUE TEST",
      "city": "Abidjan",
      "countryCode": "CI"
    }
  }'

# Health check
curl http://localhost:8080/api/v1/qr/health
```

### Avec Swagger :
Ouvrir : http://localhost:8080/swagger-ui.html

## 🎯 Résumé

| Aspect | Module (Librairie) | Application |
|--------|-------------------|-------------|
| **Rôle** | Fournit des fonctionnalités | Utilise les fonctionnalités |
| **Main** | ❌ NON | ✅ OUI |
| **Controller** | ❌ Optionnel | ✅ OUI |
| **Port** | ❌ NON | ✅ 8080 |
| **Démarrage** | ❌ Ne se lance pas | ✅ java -jar ou mvn spring-boot:run |
| **Distribution** | JAR dans Maven | JAR exécutable |
| **Utilisation** | Importé comme dépendance | Lancé comme serveur |

## 💡 Pour vos projets futurs

Dans CHACUN de vos projets Spring Boot, vous pourrez simplement :

1. **Ajouter la dépendance** dans pom.xml
2. **Injecter le service** : `@Autowired UemoaQRService`
3. **L'utiliser** dans vos controllers

**Sans avoir à :**
- ❌ Copier/coller le code
- ❌ Maintenir plusieurs versions
- ❌ Refaire les tests

C'est ça la puissance d'un module réutilisable ! 🚀
