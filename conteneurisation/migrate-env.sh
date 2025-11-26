#!/bin/bash
set -e

echo "Migration du fichier .env vers la nouvelle version"
echo "===================================================="
echo ""

# Vérification de la présence du fichier .env
if [ ! -f .env ]; then
    echo "[ERREUR] Aucun fichier .env trouvé."
    echo "Si c'est une nouvelle installation, lancez plutôt : ./setup-environment.sh"
    exit 1
fi

# Vérification de la présence du fichier .env.sample
if [ ! -f .env.sample ]; then
    echo "[ERREUR] Fichier .env.sample introuvable."
    echo "Impossible de procéder à la migration."
    exit 1
fi

# Création d'une sauvegarde
BACKUP_FILE=".env.backup.$(date +%Y%m%d_%H%M%S)"
cp .env "$BACKUP_FILE"
echo "[OK] Sauvegarde créée : $BACKUP_FILE"
echo ""

# Chargement des variables actuelles
set -a
source .env 2>/dev/null || true
set +a

# Fonction pour vérifier si une variable existe dans le .env
variable_exists() {
    grep -q "^$1=" .env 2>/dev/null
}

# Fonction pour ajouter ou mettre à jour une variable
update_or_add_variable() {
    local var_name=$1
    local var_value=$2
    
    if variable_exists "$var_name"; then
        echo "   - $var_name : déjà présent, conservation de la valeur actuelle"
    else
        echo "$var_name=$var_value" >> .env
        echo "   - $var_name : ajouté"
    fi
}

echo "Analyse des variables manquantes..."
echo ""

# Variables à ajouter/vérifier
VARS_ADDED=0

# GitHub credentials (obligatoires)
if ! variable_exists "GITHUB_USERNAME"; then
    echo "[REQUIS] Configuration des identifiants GitHub"
    echo "----------------------------------------------"
    echo "Ces identifiants sont nécessaires pour accéder aux packages GitHub."
    echo ""
    
    read -p "Nom d'utilisateur GitHub : " GITHUB_USERNAME
    while [ -z "$GITHUB_USERNAME" ]; do
        echo "[ERREUR] Le nom d'utilisateur ne peut pas être vide"
        read -p "Nom d'utilisateur GitHub : " GITHUB_USERNAME
    done
    
    read -s -p "Token GitHub (caché) : " GITHUB_TOKEN
    echo ""
    while [ -z "$GITHUB_TOKEN" ]; do
        echo "[ERREUR] Le token ne peut pas être vide"
        read -s -p "Token GitHub (caché) : " GITHUB_TOKEN
        echo ""
    done
    
    echo "GITHUB_USERNAME=$GITHUB_USERNAME" >> .env
    echo "GITHUB_TOKEN=$GITHUB_TOKEN" >> .env
    echo "[OK] Identifiants GitHub ajoutés"
    echo ""
    VARS_ADDED=$((VARS_ADDED + 2))
fi

# Nouvelles variables mailer
echo "Vérification des variables mailer..."
update_or_add_variable "SPRING_MAIL_AUTH" "true"
update_or_add_variable "SPRING_MAIL_STARTTLS" "true"
update_or_add_variable "SPRING_MAIL_HOST" "mailer"
update_or_add_variable "SPRING_MAIL_PORT" "1025"
update_or_add_variable "SPRING_MAIL_USER" "jobs@loamok.org"
update_or_add_variable "SPRING_MAIL_PASS" "MailerP@ssw0rd"
echo ""

# Nouvelles variables diverses
echo "Vérification des autres nouvelles variables..."
update_or_add_variable "CORS_ORIGINS" "${CORS_ALLOWED_ORIGINS}"
update_or_add_variable "BASE_PROTOCOL" "http"
update_or_add_variable "BASE_URL" "''"
update_or_add_variable "DB_HOST" "db"
echo ""

# Vérification de la variable PORT vs BACKEND_PORT
if variable_exists "PORT" && ! variable_exists "BACKEND_PORT"; then
    PORT_VALUE=$(grep "^PORT=" .env | cut -d'=' -f2)
    echo "BACKEND_PORT=$PORT_VALUE" >> .env
    echo "[OK] BACKEND_PORT ajouté avec la valeur de PORT ($PORT_VALUE)"
    echo "    Note : PORT est maintenant BACKEND_PORT, mais PORT est conservé pour compatibilité"
    VARS_ADDED=$((VARS_ADDED + 1))
    echo ""
fi

# Récapitulatif
echo "=========================================="
echo "Migration terminée !"
echo "=========================================="
echo ""
echo "Résumé des changements :"
echo "   - Sauvegarde : $BACKUP_FILE"
echo "   - Variables ajoutées/vérifiées : voir ci-dessus"
echo ""

# Affichage des différences si possible
if command -v diff &> /dev/null; then
    echo "Différences avec l'ancienne version :"
    echo "--------------------------------------"
    diff "$BACKUP_FILE" .env || true
    echo ""
fi

echo "Prochaines étapes :"
echo "   1. Vérifiez le fichier .env pour vous assurer que tout est correct"
echo "   2. Lancez : ./build-and-run.sh"
echo ""
echo "En cas de problème, vous pouvez restaurer la sauvegarde :"
echo "   cp $BACKUP_FILE .env"
echo ""
