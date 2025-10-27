#!/bin/bash
set -e

echo "Configuration de l'environnement"
echo "===================================="

# Demande le nom du projet si pas déjà défini
if [ -f .env ]; then
    echo " Un fichier .env existe déjà."
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
        echo "Fichier .env créé à partir de .env.sample"
    else
        echo "Fichier .env.sample introuvable"
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
    echo "COMPOSE_PROJECT_NAME mis à jour : $PROJECT_NAME"
else
    # Ajoute au début du fichier
    echo "COMPOSE_PROJECT_NAME=$PROJECT_NAME" | cat - .env > .env.tmp && mv .env.tmp .env
    echo "COMPOSE_PROJECT_NAME ajouté : $PROJECT_NAME"
fi

echo ""
echo "Résumé de la configuration :"
echo "   - Nom du projet : $PROJECT_NAME"
echo "   - Conteneurs   : ${PROJECT_NAME}_backend, ${PROJECT_NAME}_frontend, ${PROJECT_NAME}_db"
echo "   - Volumes      : ${PROJECT_NAME}_postgresql_data, ${PROJECT_NAME}_pgadmin_*"
echo "   - Réseau       : ${PROJECT_NAME}_network"
echo ""
echo "Configuration terminée !"
echo ""
echo "Prochaines étapes :"
echo "   1. Éditez le fichier .env pour configurer les secrets"
echo "   2. Lancez : ./build-and-run.sh"