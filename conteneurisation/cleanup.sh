#!/bin/bash
set -e

echo "Nettoyage de l'environnement Docker"
echo "======================================="

# Charge le .env pour obtenir le nom du projet
if [ -f .env ]; then
    set -a
    source .env
    set +a
else
    echo "[ERREUR] Fichier .env introuvable"
    exit 1
fi

PROJECT_NAME=${COMPOSE_PROJECT_NAME:-jobs}

echo ""
echo "Projet détecté : $PROJECT_NAME"
echo ""
echo "Cette action va :"
echo "  - Arrêter tous les conteneurs de $PROJECT_NAME"
echo "  - Supprimer tous les conteneurs de $PROJECT_NAME"
echo "  - Supprimer tous les volumes de données de $PROJECT_NAME"
echo "  - /!\ TOUTES LES DONNÉES DE LA BASE SERONT PERDUES !"
echo ""

read -p "Êtes-vous sûr de vouloir continuer ? (yes/N): " confirm

if [[ ! $confirm == "yes" ]]; then
    echo "Nettoyage annulé."
    exit 0
fi

echo ""
echo "Arrêt des conteneurs..."
docker compose down -v

echo ""
echo "Suppression des conteneurs orphelins..."
docker ps -a --filter "name=${PROJECT_NAME}" --format "{{.ID}}" | xargs -r docker rm -f 2>/dev/null || true

echo ""
echo "Suppression des volumes..."
docker volume ls --filter "name=${PROJECT_NAME}" --format "{{.Name}}" | xargs -r docker volume rm 2>/dev/null || true

echo ""
echo "Suppression du réseau..."
docker network ls --filter "name=${PROJECT_NAME}" --format "{{.ID}}" | xargs -r docker network rm 2>/dev/null || true

echo ""
echo "[OK] Nettoyage terminé !"
echo ""
echo "Pour redémarrer proprement :"
echo "   ./build-and-run.sh"
