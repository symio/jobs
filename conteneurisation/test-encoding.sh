#!/bin/bash
set -e

echo "🧪 Test d'encodage/décodage de la variable UNENCODED_KEY"
echo "===================================================="

# Charge le .env
if [ -f .env ]; then
    set -a
    source .env
    set +a
else
    echo "Fichier .env introuvable"
    exit 1
fi

echo ""
echo "📋 Valeur originale :"
echo "$UNENCODED_KEY"

echo ""
echo "🔐 Encodage en base64 :"
# Encode en base64 (compatible Linux et macOS)
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    ENCODED=$(echo -n "$UNENCODED_KEY" | base64)
else
    # Linux (et Git Bash sur Windows)
    ENCODED=$(echo -n "$UNENCODED_KEY" | base64 | tr -d '\n')
fi
echo "$ENCODED"

echo ""
echo "🔓 Décodage pour vérification :"
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    DECODED=$(echo "$ENCODED" | base64 -D)
else
    # Linux (et Git Bash sur Windows)
    DECODED=$(echo "$ENCODED" | base64 -d)
fi
echo "$DECODED"

echo ""
if [ "$UNENCODED_KEY" = "$DECODED" ]; then
    echo "SUCCESS : L'encodage/décodage fonctionne parfaitement !"
    echo "Tous les caractères spéciaux sont préservés"
else
    echo "ERREUR : La valeur décodée ne correspond pas à l'original"
    echo "   Vérifie que UNENCODED_KEY utilise des guillemets simples dans .env"
    exit 1
fi

echo ""
echo "📊 Statistiques :"
echo "   - Longueur originale : ${#UNENCODED_KEY} caractères"
echo "   - Longueur base64    : ${#ENCODED} caractères"
echo ""
echo "🖥️  OS détecté : $OSTYPE"
