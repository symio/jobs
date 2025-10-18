#!/bin/bash
set -e

echo "🚀Build et démarrage de la stack Docker"
echo "========================================"

# Fonction pour trouver l'IP locale (Linux/macOS)
get_local_ip() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        ipconfig getifaddr en0
    else
        hostname -I | awk '{print $1}'
    fi
}

echo ""
echo "🔗Étape 0/4 : Mise à jour des origines CORS avec l'IP locale"

LOCAL_IP=$(get_local_ip)
if [ -z "$LOCAL_IP" ]; then
    echo "/!\ Impossible de détecter l'IP locale. L'accès depuis d'autres appareils pourrait ne pas fonctionner."
else
    echo "   - IP locale détectée : $LOCAL_IP"
    DYNAMIC_ORIGIN="http://${LOCAL_IP}"

    # Origines de base fixes
    BASE_ORIGINS="http://localhost,http://frontend"

    # Construction de la variable CORS finale
    UPDATED_ORIGINS="${BASE_ORIGINS},${DYNAMIC_ORIGIN}"

    # Mise à jour du .env
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s|^CORS_ALLOWED_ORIGINS=.*|CORS_ALLOWED_ORIGINS='${UPDATED_ORIGINS}'|" .env
    else
        sed -i "s|^CORS_ALLOWED_ORIGINS=.*|CORS_ALLOWED_ORIGINS='${UPDATED_ORIGINS}'|" .env
    fi

    echo "   - CORS_ALLOWED_ORIGINS mis à jour : $UPDATED_ORIGINS"
fi

# Étape 1 : Préparer le .env avec l'encodage base64
echo ""
echo "📝 Étape 1/3 : Préparation du fichier .env"
./encode-env.sh

# Étape 2 : Build des images
echo ""
echo "🔨 Étape 2/3 : Build des images Docker"
docker compose build --no-cache

# Étape 3 : Démarrage de la stack
echo ""
echo "🚀 Étape 3/3 : Démarrage de la stack"
docker compose up -d

echo ""
echo "Stack démarrée avec succès !"
echo ""
echo "📊 Services disponibles :"
echo "   - Backend:  http://localhost:8080"
echo "   - Frontend: http://localhost:80, ou http://localhost,"
echo "   - PgAdmin:  http://localhost:5433"
if [ -n "$LOCAL_IP" ]; then
    echo "   - Accès depuis le réseau local: http://${LOCAL_IP}"
fi
echo ""
echo "📝Pour voir les logs : docker compose logs -f"
