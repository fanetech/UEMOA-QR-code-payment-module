#!/bin/bash

# Script de déploiement automatisé pour UEMOA QR Module
# Usage: ./deploy.sh [release|snapshot]

set -e

echo "======================================"
echo "Déploiement UEMOA QR Module"
echo "======================================"

# Variables
NEXUS_URL=${NEXUS_URL:-"http://localhost:8081"}
NEXUS_USER=${NEXUS_USER:-"admin"}
NEXUS_PASS=${NEXUS_PASS:-"admin123"}
VERSION_TYPE=${1:-"snapshot"}

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Vérifier que Maven est installé
if ! command -v mvn &> /dev/null; then
    log_error "Maven n'est pas installé"
    exit 1
fi

# Nettoyer et compiler
log_info "Nettoyage et compilation..."
mvn clean compile

# Exécuter les tests
log_info "Exécution des tests..."
mvn test

if [ $? -ne 0 ]; then
    log_error "Les tests ont échoué. Arrêt du déploiement."
    exit 1
fi

# Déterminer la version
CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
log_info "Version actuelle: $CURRENT_VERSION"

if [ "$VERSION_TYPE" = "release" ]; then
    # Pour une release, retirer -SNAPSHOT si présent
    if [[ $CURRENT_VERSION == *"-SNAPSHOT"* ]]; then
        NEW_VERSION=${CURRENT_VERSION%-SNAPSHOT}
        log_info "Changement de version pour release: $NEW_VERSION"
        mvn versions:set -DnewVersion=$NEW_VERSION
        mvn versions:commit
    fi
elif [ "$VERSION_TYPE" = "snapshot" ]; then
    # Pour un snapshot, ajouter -SNAPSHOT si absent
    if [[ $CURRENT_VERSION != *"-SNAPSHOT"* ]]; then
        NEW_VERSION="${CURRENT_VERSION}-SNAPSHOT"
        log_info "Changement de version pour snapshot: $NEW_VERSION"
        mvn versions:set -DnewVersion=$NEW_VERSION
        mvn versions:commit
    fi
else
    log_error "Type de version invalide. Utilisez 'release' ou 'snapshot'"
    exit 1
fi

# Créer le settings.xml temporaire
TEMP_SETTINGS=$(mktemp)
cat > $TEMP_SETTINGS << EOF
<settings>
    <servers>
        <server>
            <id>nexus-releases</id>
            <username>$NEXUS_USER</username>
            <password>$NEXUS_PASS</password>
        </server>
        <server>
            <id>nexus-snapshots</id>
            <username>$NEXUS_USER</username>
            <password>$NEXUS_PASS</password>
        </server>
    </servers>
</settings>
EOF

# Déployer
log_info "Déploiement sur Nexus..."
mvn deploy -s $TEMP_SETTINGS -DaltDeploymentRepository=nexus-${VERSION_TYPE}s::default::${NEXUS_URL}/repository/maven-${VERSION_TYPE}s/

if [ $? -eq 0 ]; then
    log_info "✅ Déploiement réussi!"
    log_info "Le module est maintenant disponible sur: ${NEXUS_URL}"
    
    # Afficher comment l'utiliser
    FINAL_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
    echo ""
    echo "Pour utiliser ce module dans vos projets, ajoutez:"
    echo ""
    echo "<dependency>"
    echo "    <groupId>com.aveplus</groupId>"
    echo "    <artifactId>uemoa-qrcode-module</artifactId>"
    echo "    <version>$FINAL_VERSION</version>"
    echo "</dependency>"
else
    log_error "❌ Le déploiement a échoué"
    exit 1
fi

# Nettoyer
rm -f $TEMP_SETTINGS

echo ""
echo "======================================"
echo "Déploiement terminé"
echo "======================================"
