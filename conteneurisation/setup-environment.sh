#!/bin/bash
set -e

echo "Configuration de l'environnement"
echo "===================================="

# Demande le nom du projet si pas déjà défini
if [ -f .env ]; then
    echo "Un fichier .env existe déjà."
    read -p "Voulez-vous le reconfigurer ? (y/N): " reconfigure
    if [[ ! $reconfigure =~ ^[Yy]$ ]]; then
        echo "Configuration conservée."
        exit 0
    fi
fi

# Demande le nom du projet
read -p "Nom du projet (ex: jobs-dev, jobs-test, jobs-prod) [jobs]: " PROJECT_NAME
PROJECT_NAME=${PROJECT_NAME:-jobs}

# Nettoie le nom du projet (remplace les espaces et caractères spéciaux)
PROJECT_NAME=$(echo "$PROJECT_NAME" | tr ' ' '-' | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9-]//g')

echo ""
echo "Configuration du projet : $PROJECT_NAME"
echo ""

# Copie le fichier sample si .env n'existe pas
if [ ! -f .env ]; then
    if [ -f .env.sample ]; then
        cp .env.sample .env
        echo "[OK] Fichier .env créé à partir de .env.sample"
    else
        echo "[ERREUR] Fichier .env.sample introuvable"
        exit 1
    fi
fi

# Met à jour ou ajoute COMPOSE_PROJECT_NAME
if grep -q "^COMPOSE_PROJECT_NAME=" .env; then
    # Remplace la ligne existante
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s|^COMPOSE_PROJECT_NAME=.*|COMPOSE_PROJECT_NAME=$PROJECT_NAME|" .env
    else
        sed -i "s|^COMPOSE_PROJECT_NAME=.*|COMPOSE_PROJECT_NAME=$PROJECT_NAME|" .env
    fi
    echo "[OK] COMPOSE_PROJECT_NAME mis à jour : $PROJECT_NAME"
else
    # Ajoute au début du fichier
    echo "COMPOSE_PROJECT_NAME=$PROJECT_NAME" | cat - .env > .env.tmp && mv .env.tmp .env
    echo "[OK] COMPOSE_PROJECT_NAME ajouté : $PROJECT_NAME"
fi

# Configuration GitHub
echo ""
echo "Configuration des identifiants GitHub"
echo "--------------------------------------"
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

# Met à jour GITHUB_USERNAME
if grep -q "^GITHUB_USERNAME=" .env; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s|^GITHUB_USERNAME=.*|GITHUB_USERNAME=$GITHUB_USERNAME|" .env
    else
        sed -i "s|^GITHUB_USERNAME=.*|GITHUB_USERNAME=$GITHUB_USERNAME|" .env
    fi
    echo "[OK] GITHUB_USERNAME mis à jour"
else
    echo "GITHUB_USERNAME=$GITHUB_USERNAME" >> .env
    echo "[OK] GITHUB_USERNAME ajouté"
fi

# Met à jour GITHUB_TOKEN
if grep -q "^GITHUB_TOKEN=" .env; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s|^GITHUB_TOKEN=.*|GITHUB_TOKEN=$GITHUB_TOKEN|" .env
    else
        sed -i "s|^GITHUB_TOKEN=.*|GITHUB_TOKEN=$GITHUB_TOKEN|" .env
    fi
    echo "[OK] GITHUB_TOKEN mis à jour"
else
    echo "GITHUB_TOKEN=$GITHUB_TOKEN" >> .env
    echo "[OK] GITHUB_TOKEN ajouté"
fi

echo ""
echo "Résumé de la configuration :"
echo "   - Nom du projet : $PROJECT_NAME"
echo "   - Conteneurs   : ${PROJECT_NAME}_backend, ${PROJECT_NAME}_frontend, ${PROJECT_NAME}_db"
echo "   - Volumes      : ${PROJECT_NAME}_postgresql_data, ${PROJECT_NAME}_pgadmin_*, ${PROJECT_NAME}_mailer_data"
echo "   - Réseau       : ${PROJECT_NAME}_network"
echo ""
echo "[OK] Configuration terminée avec succès !"
echo ""
echo "Prochaines étapes :"
echo "   1. Vérifiez le fichier .env pour ajuster d'autres paramètres si nécessaire"
echo "   2. Lancez : ./build-and-run.sh"