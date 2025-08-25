# Guide de déploiement sur Nexus/Artifactory

## 📋 Prérequis

1. **Serveur Nexus ou Artifactory** configuré et accessible
2. **Compte utilisateur** avec droits de déploiement
3. **Maven** installé sur votre machine

## 🔧 Configuration

### 1. Configuration pour Nexus

#### Étape 1 : Modifier le pom.xml

Remplacez `YOUR-NEXUS-URL` dans le pom.xml par l'URL de votre serveur Nexus :

```xml
<distributionManagement>
    <repository>
        <id>nexus-releases</id>
        <url>http://nexus.votre-entreprise.com:8081/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>nexus-snapshots</id>
        <url>http://nexus.votre-entreprise.com:8081/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

#### Étape 2 : Configurer les credentials

Copiez le fichier `settings-nexus.xml` dans votre dossier `.m2` :

```bash
# Linux/Mac
cp settings-nexus.xml ~/.m2/settings.xml

# Windows
copy settings-nexus.xml %USERPROFILE%\.m2\settings.xml
```

Puis éditez le fichier pour ajouter vos identifiants :

```xml
<server>
    <id>nexus-releases</id>
    <username>votre-username</username>
    <password>votre-password</password>
</server>
```

### 2. Configuration pour Artifactory

#### Étape 1 : Modifier le pom.xml

```xml
<distributionManagement>
    <repository>
        <id>artifactory-releases</id>
        <url>http://artifactory.votre-entreprise.com/artifactory/libs-release-local</url>
    </repository>
    <snapshotRepository>
        <id>artifactory-snapshots</id>
        <url>http://artifactory.votre-entreprise.com/artifactory/libs-snapshot-local</url>
    </snapshotRepository>
</distributionManagement>
```

#### Étape 2 : Configurer les credentials

Dans `~/.m2/settings.xml` :

```xml
<server>
    <id>artifactory-releases</id>
    <username>votre-username</username>
    <password>votre-password-ou-token-api</password>
</server>
```

## 🚀 Déploiement

### Déployer une version Release

```bash
# S'assurer que la version dans pom.xml n'est pas SNAPSHOT
# Version: 1.0.0 (sans -SNAPSHOT)

# Nettoyer et compiler
mvn clean compile

# Exécuter les tests
mvn test

# Déployer sur Nexus/Artifactory
mvn deploy
```

### Déployer une version Snapshot

```bash
# S'assurer que la version dans pom.xml est SNAPSHOT
# Version: 1.0.0-SNAPSHOT

# Déployer
mvn clean deploy
```

### Déploiement avec profil spécifique

```bash
# Si vous avez plusieurs repositories configurés
mvn deploy -P nexus

# Ou
mvn deploy -P artifactory
```

### Déploiement en sautant les tests

```bash
mvn clean deploy -DskipTests
```

### Déploiement avec settings.xml personnalisé

```bash
mvn deploy -s /path/to/custom-settings.xml
```

## 🔒 Sécurité

### Option 1 : Chiffrer les mots de passe Maven

```bash
# Créer un master password
mvn --encrypt-master-password

# Chiffrer votre mot de passe
mvn --encrypt-password
```

### Option 2 : Utiliser des variables d'environnement

Dans `settings.xml` :

```xml
<server>
    <id>nexus-releases</id>
    <username>${env.NEXUS_USERNAME}</username>
    <password>${env.NEXUS_PASSWORD}</password>
</server>
```

Puis définir les variables :

```bash
export NEXUS_USERNAME=your-username
export NEXUS_PASSWORD=your-password
mvn deploy
```

### Option 3 : Utiliser un token API (Artifactory)

```xml
<server>
    <id>artifactory-releases</id>
    <username>your-email@company.com</username>
    <password>API-TOKEN-HERE</password>
</server>
```

## 📝 Vérification après déploiement

### Sur Nexus

1. Connectez-vous à l'interface web Nexus
2. Allez dans "Browse" → "maven-releases" ou "maven-snapshots"
3. Cherchez : `com/aveplus/uemoa-qrcode-module`

### Sur Artifactory

1. Connectez-vous à l'interface web Artifactory
2. Allez dans "Artifacts" → "libs-release-local" ou "libs-snapshot-local"
3. Cherchez : `com/aveplus/uemoa-qrcode-module`

## 🔄 CI/CD avec GitHub Actions

Créez `.github/workflows/deploy.yml` :

```yaml
name: Deploy to Nexus

on:
  push:
    tags:
      - 'v*'

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'adopt'
    
    - name: Deploy to Nexus
      env:
        NEXUS_USERNAME: ${{ secrets.NEXUS_USERNAME }}
        NEXUS_PASSWORD: ${{ secrets.NEXUS_PASSWORD }}
      run: |
        mkdir -p ~/.m2
        echo "<settings>
          <servers>
            <server>
              <id>nexus-releases</id>
              <username>${NEXUS_USERNAME}</username>
              <password>${NEXUS_PASSWORD}</password>
            </server>
          </servers>
        </settings>" > ~/.m2/settings.xml
        
        mvn clean deploy
```

## 🔄 CI/CD avec GitLab CI

Créez `.gitlab-ci.yml` :

```yaml
deploy:
  stage: deploy
  script:
    - echo "<settings>
        <servers>
          <server>
            <id>nexus-releases</id>
            <username>${NEXUS_USERNAME}</username>
            <password>${NEXUS_PASSWORD}</password>
          </server>
        </servers>
      </settings>" > ~/.m2/settings.xml
    - mvn clean deploy
  only:
    - tags
```

## 📊 Utilisation après déploiement

Dans les autres projets, ajoutez le repository dans le pom.xml :

```xml
<repositories>
    <repository>
        <id>nexus-releases</id>
        <url>http://nexus.votre-entreprise.com:8081/repository/maven-releases/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.aveplus</groupId>
        <artifactId>uemoa-qrcode-module</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

## ❌ Résolution des problèmes

### Erreur 401 Unauthorized

- Vérifiez vos identifiants dans settings.xml
- Vérifiez que l'ID dans settings.xml correspond à celui du pom.xml
- Vérifiez les permissions de votre compte sur Nexus/Artifactory

### Erreur 405 Method Not Allowed

- Vous essayez de déployer un SNAPSHOT sur un repository de releases (ou vice versa)
- Vérifiez la version dans pom.xml

### Erreur de connexion

```bash
# Tester la connexion
curl -u username:password http://nexus-url/service/rest/v1/status
```

### Logs détaillés

```bash
mvn deploy -X
```

## 📚 Ressources

- [Documentation Nexus](https://help.sonatype.com/repomanager3)
- [Documentation Artifactory](https://www.jfrog.com/confluence/display/JFROG/JFrog+Artifactory)
- [Maven Deploy Plugin](https://maven.apache.org/plugins/maven-deploy-plugin/)
