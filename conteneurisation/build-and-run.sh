#!/bin/bash
set -e

echo "🚀 Build et démarrage de la stack Docker"
echo "========================================"

# Fonction pour trouver l'IP locale (compatible Linux/macOS)
get_local_ip() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        ipconfig getifaddr en0
    else
        # Linux
        hostname -I | awk '{print $1}'
    fi
}

echo ""
echo "🔗 Étape 0/4 : Ajout de l'IP locale aux origines CORS"

LOCAL_IP=$(get_local_ip)
if [ -z "$LOCAL_IP" ]; then
    echo "/!\Impossible de détecter l'IP locale. L'accès depuis d'autres appareils pourrait ne pas fonctionner."
else
    CORS_URL_BASE="http://${LOCAL_IP}"
    CORS_URL="${CORS_URL_BASE}:4200"
    echo "   - IP locale détectée : $LOCAL_IP"
    echo "   - URL CORS à ajouter : ${CORS_URL_BASE},${CORS_URL}"
    
    # 1. Lire les origines actuelles depuis le .env et retirer les quotes
    CURRENT_ORIGINS=$(grep "^CORS_ALLOWED_ORIGINS=" .env | cut -d'=' -f2- | tr -d "'")

    # 2. Utiliser un tableau pour gérer la liste
    OLD_IFS=$IFS
    IFS=','
    read -ra ORIGIN_ARRAY <<< "$CURRENT_ORIGINS"
    IFS=$OLD_IFS # Restaure l'ancien IFS

    # 3. Déterminer la taille du tableau
    ARRAY_SIZE=${#ORIGIN_ARRAY[@]}
    
    # 4. Déterminer si un nettoyage est nécessaire.
    if [ "$ARRAY_SIZE" -gt 3 ]; then
        # On garde tous les éléments sauf les 2 derniers
        ELEMENTS_TO_KEEP=$(( ARRAY_SIZE - 2 ))
        
        # 5. Extraire les éléments à conserver (les "base origins" permanentes)
        BASE_ORIGINS=$(printf '%s,' "${ORIGIN_ARRAY[@]:0:ELEMENTS_TO_KEEP}")
        BASE_ORIGINS=${BASE_ORIGINS%,} # Retirer la virgule finale
        
        echo "   - Nettoyage : Suppression des 2 dernières entrées."
    else
        BASE_ORIGINS="$CURRENT_ORIGINS"
        echo "   - Nettoyage : Taille inférieure ou égale à 3, conservation de la liste existante."
    fi
    
    # 6. Construction des nouvelles origines complètes
    if [[ ! "$CURRENT_ORIGINS" == *"$CORS_URL"* ]]; then
        if [ -n "$BASE_ORIGINS" ]; then
            UPDATED_ORIGINS="${BASE_ORIGINS},${CORS_URL_BASE},${CORS_URL}"
        else
            UPDATED_ORIGINS="${CORS_URL_BASE},${CORS_URL}"
        fi
        
        # 7. Mettre à jour le fichier .env (compatible Linux/macOS)
        echo "   - Origines de base conservées : '$BASE_ORIGINS'"
        echo "   - Nouvelles origines complètes : '$UPDATED_ORIGINS'"

        if [[ "$OSTYPE" == "darwin"* ]]; then
            sed -i '' "s|^CORS_ALLOWED_ORIGINS=.*|CORS_ALLOWED_ORIGINS='${UPDATED_ORIGINS}'|" .env
        else
            sed -i "s|^CORS_ALLOWED_ORIGINS=.*|CORS_ALLOWED_ORIGINS='${UPDATED_ORIGINS}'|" .env
        fi
        echo "   - Fichier .env mis à jour avec la nouvelle URL."
    else
        echo "   - L'IP locale est déjà la dernière, aucune modification nécessaire."
    fi
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
echo "  Étape 3/3 : Démarrage de la stack"
docker compose up -d

echo ""
echo "Stack démarrée avec succès !"
echo ""
echo "📊 Services disponibles :"
echo "   - Backend:  http://localhost:8080"
echo "   - Frontend: http://localhost:4200"
echo "   - PgAdmin:  http://localhost:5433"
echo ""
echo "📝 Pour voir les logs : docker compose logs -f"
