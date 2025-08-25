# 📚 Guide Complet - Publication sur Maven Central

## 🎯 Objectif
Publier le module `uemoa-qrcode-module` sur Maven Central pour qu'il soit accessible publiquement comme les packages NPM.

## 📋 Checklist Complète pour le Stagiaire

### ✅ Phase 1 : Préparation (Jour 1-3)

#### 1.1 Créer un compte Sonatype JIRA
```
1. Aller sur : https://issues.sonatype.org
2. Cliquer sur "Sign Up"
3. Remplir le formulaire avec email professionnel
4. Confirmer l'email
5. Se connecter
```

#### 1.2 Créer un ticket JIRA pour nouveau projet
```
Projet : Community Support - Open Source Project Repository Hosting (OSSRH)
Type : New Project
Résumé : Publish com.aveplus artifacts to Maven Central

Description :
I would like to publish my open source library to Maven Central.

Group Id : com.aveplus
Project URL : https://github.com/aveplus/uemoa-qrcode-module
SCM URL : https://github.com/aveplus/uemoa-qrcode-module.git

Description du projet :
UEMOA QR Code Payment Module - Spring Boot module for generating and parsing 
payment QR codes in the WAEMU region according to EMVCo and BCEAO standards.
```

**⏱️ Attendre la réponse (24-48h)**

### ✅ Phase 2 : Configuration GPG (Jour 3-4)

#### 2.1 Installer GPG

**macOS :**
```bash
brew install gnupg
```

**Linux :**
```bash
sudo apt-get install gnupg
```

**Windows :**
Télécharger depuis : https://www.gnupg.org/download/

#### 2.2 Générer une clé GPG
```bash
# Générer la clé
gpg --gen-key

# Suivre les instructions :
# - Real n : Votre nom
# - Email : dev@aveplus.com
# - Passphrase : [mot de passe sécurisé]
```

#### 2.3 Lister et publier la clé
```bash
# Lister les clés
gpg --list-keys

# Noter le KEY_ID (8 derniers caractères)
# Exemple : 1234ABCD

# Publier sur les serveurs
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
gpg --keyserver pgp.mit.edu --send-keys YOUR_KEY_ID
```

### ✅ Phase 3 : Configuration Maven (Jour 4-5)

#### 3.1 Créer/Modifier ~/.m2/settings.xml
```xml
<settings>
  <servers>
    <!-- Credentials Sonatype -->
    <server>
      <id>ossrh</id>
      <username>YOUR_SONATYPE_USERNAME</username>
      <password>YOUR_SONATYPE_PASSWORD</password>
    </server>
  </servers>
  
  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>YOUR_GPG_PASSPHRASE</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

#### 3.2 Remplacer le pom.xml
```bash
# Sauvegarder l'ancien
cp pom.xml pom-backup.xml

# Utiliser le nouveau
cp pom-maven-central.xml pom.xml
```

### ✅ Phase 4 : Préparation du Code (Jour 5-7)

#### 4.1 Ajouter les fichiers obligatoires

**LICENSE :**
```bash
curl -o LICENSE https://www.apache.org/licenses/LICENSE-2.0.txt
```

**CHANGELOG.md :**
```markdown
# Changelog

## [1.0.0] - 2024-XX-XX
### Added
- Initial release
- Static QR code generation
- Dynamic QR code generation
- P2P QR code generation
- EMVCo QR code parsing
- CRC16-CCITT validation
- Support for all WAEMU countries
```

#### 4.2 Améliorer la Javadoc
```java
/**
 * Service principal pour la gestion des QR codes UEMOA.
 * 
 * @author AvePlus Team
 * @since 1.0.0
 */
```

#### 4.3 Tests et Couverture
```bash
# Lancer les tests avec couverture
mvn clean test jacoco:report

# Vérifier le rapport
open target/site/jacoco/index.html
```

### ✅ Phase 5 : Déploiement SNAPSHOT (Jour 7-8)

#### 5.1 Version SNAPSHOT
```bash
# Vérifier que la version contient -SNAPSHOT
# Dans pom.xml : <version>1.0.0-SNAPSHOT</version>

# Déployer
mvn clean deploy -P release
```

#### 5.2 Vérifier sur Sonatype
```
1. Aller sur : https://oss.sonatype.org
2. Se connecter avec vos identifiants Sonatype
3. Cliquer sur "Staging Repositories"
4. Chercher votre repository (comaveplus-XXXX)
5. Vérifier les artifacts
```

### ✅ Phase 6 : Release Finale (Jour 9-10)

#### 6.1 Préparer la version release
```bash
# Retirer -SNAPSHOT de la version
mvn versions:set -DnewVersion=1.0.0
mvn versions:commit

# Commit et tag
git add -A
git commit -m "Release version 1.0.0"
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin main --tags
```

#### 6.2 Déployer la release
```bash
# Nettoyer et déployer
mvn clean deploy -P release
```

#### 6.3 Promouvoir sur Maven Central
```
1. Aller sur : https://oss.sonatype.org
2. "Staging Repositories"
3. Sélectionner votre repository
4. Cliquer sur "Close" (attendre validation)
5. Cliquer sur "Release"
```

### ✅ Phase 7 : Vérification (Jour 10)

#### 7.1 Attendre la synchronisation (2-4 heures)

#### 7.2 Vérifier sur Maven Central
```
https://search.maven.org/search?q=g:com.aveplus
```

#### 7.3 Tester l'utilisation
```xml
<dependency>
    <groupId>com.aveplus</groupId>
    <artifactId>uemoa-qrcode-module</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🚀 Script d'Automatisation

```bash
#!/bin/bash
# deploy-maven-central.sh

echo "🚀 Déploiement sur Maven Central"

# Vérifications
command -v mvn >/dev/null 2>&1 || { echo "Maven non installé"; exit 1; }
command -v gpg >/dev/null 2>&1 || { echo "GPG non installé"; exit 1; }

# Tests
echo "📝 Exécution des tests..."
mvn clean test

# Version
read -p "Version à déployer (ex: 1.0.0): " VERSION
mvn versions:set -DnewVersion=$VERSION

# Déploiement
echo "📦 Déploiement..."
mvn clean deploy -P release

echo "✅ Déployé ! Vérifier sur https://oss.sonatype.org"
```

## 🔧 Troubleshooting

### Erreur : gpg: signing failed
```bash
export GPG_TTY=$(tty)
```

### Erreur : 401 Unauthorized
- Vérifier username/password dans settings.xml
- Vérifier que le compte Sonatype est activé

### Erreur : Repository not found
- Attendre que le ticket JIRA soit approuvé
- Vérifier le groupId

## 📊 Métriques de Succès

- [ ] Ticket JIRA approuvé
- [ ] Tests passent à 100%
- [ ] Couverture de code > 70%
- [ ] Javadoc complète
- [ ] Déployé sur staging
- [ ] Visible sur Maven Central
- [ ] Badge ajouté au README

## 🎯 Résultat Final

Une fois publié, les développeurs pourront utiliser :

```xml
<dependency>
    <groupId>com.aveplus</groupId>
    <artifactId>uemoa-qrcode-module</artifactId>
    <version>1.0.0</version>
</dependency>
```

Ou avec Gradle :
```gradle
implementation 'com.aveplus:uemoa-qrcode-module:1.0.0'
```

## 📚 Ressources

- [Guide Sonatype](https://central.sonatype.org/publish/publish-guide/)
- [Requirements](https://central.sonatype.org/publish/requirements/)
- [GPG Signing](https://central.sonatype.org/publish/requirements/gpg/)
- [OSSRH Guide](https://central.sonatype.org/publish/publish-maven/)
