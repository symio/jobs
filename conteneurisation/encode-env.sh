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
    echo "[ERREUR] Fichier .env introuvable"
    exit 1
fi

# Vérifie que la variable UNENCODED_KEY existe
if [ -z "$UNENCODED_KEY" ]; then
    echo "[ERREUR] Variable UNENCODED_KEY non définie dans .env"
    exit 1
fi

# Encode en base64 (compatible Linux et macOS)
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    JWT_SECRET=$(echo -n "$UNENCODED_KEY" | base64)
else
    # Linux (et Git Bash sur Windows)
    JWT_SECRET=$(echo -n "$UNENCODED_KEY" | base64 | tr -d '\n')
fi

# Vérifie si JWT_SECRET existe déjà dans .env
if grep -q "^JWT_SECRET=" .env 2>/dev/null; then
    # Crée un fichier temporaire (compatible cross-platform)
    grep -v "^JWT_SECRET=" .env > .env.tmp
    echo "JWT_SECRET='${JWT_SECRET}'" >> .env.tmp
    mv .env.tmp .env
    echo "[OK] Variable JWT_SECRET mise à jour dans .env"
else
    # Ajoute la nouvelle variable avec guillemets simples
    echo "" >> .env
    echo "# Auto-generated base64 encoded value" >> .env
    echo "JWT_SECRET='${JWT_SECRET}'" >> .env
    echo "[OK] Variable JWT_SECRET ajoutée au .env"
fi

echo "Valeur originale: $UNENCODED_KEY"
echo "Valeur base64: $JWT_SECRET"
echo ""
echo "[OK] Fichier .env prêt pour le build Docker"
