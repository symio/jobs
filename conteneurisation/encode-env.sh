#!/bin/bash
set -e

echo "Préparation des variables d'environnement..."

# Charge les variables du fichier .env
if [ -f .env ]; then
    # Utilise set -a pour exporter toutes les variables
    set -a
    source .env
    set +a
else
    echo "Fichier .env introuvable"
    exit 1
fi

# Vérifie que la variable UNENCODED_KEY existe
if [ -z "$UNENCODED_KEY" ]; then
    echo "Variable UNENCODED_KEY non définie dans .env"
    exit 1
fi

# Encode en base64 (compatible Linux et macOS)
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    SPRING_JWT_B64_KEY=$(echo -n "$UNENCODED_KEY" | base64)
else
    # Linux (et Git Bash sur Windows)
    SPRING_JWT_B64_KEY=$(echo -n "$UNENCODED_KEY" | base64 | tr -d '\n')
fi

# Vérifie si SPRING_JWT_B64_KEY existe déjà dans .env
if grep -q "^SPRING_JWT_B64_KEY=" .env 2>/dev/null; then
    # Crée un fichier temporaire (compatible cross-platform)
    grep -v "^SPRING_JWT_B64_KEY=" .env > .env.tmp
    echo "SPRING_JWT_B64_KEY='${SPRING_JWT_B64_KEY}'" >> .env.tmp
    mv .env.tmp .env
    echo "Variable SPRING_JWT_B64_KEY mise à jour dans .env"
else
    # Ajoute la nouvelle variable avec guillemets simples
    echo "" >> .env
    echo "# Auto-generated base64 encoded value" >> .env
    echo "SPRING_JWT_B64_KEY='${SPRING_JWT_B64_KEY}'" >> .env
    echo "Variable SPRING_JWT_B64_KEY ajoutée au .env"
fi

echo "Valeur originale: $UNENCODED_KEY"
echo "Valeur base64: $SPRING_JWT_B64_KEY"
echo ""
echo "Fichier .env prêt pour le build Docker"
