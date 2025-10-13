#!/bin/bash
set -e

echo "Attente que le backend soit prêt..."
sleep 10

echo "Vérification de l'existence de la table roles..."

# Vérifie si la table existe et est vide avant d'insérer
PGPASSWORD=$POSTGRES_PASSWORD psql -h db -U $POSTGRES_USER -d $POSTGRES_DB <<-EOSQL
    DO \$\$
    BEGIN
        -- Vérifie si la table roles existe et est vide
        IF EXISTS (
            SELECT FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_name = 'roles'
        ) THEN
            IF NOT EXISTS (SELECT 1 FROM public.roles LIMIT 1) THEN
                -- Insertion des rôles par défaut
                INSERT INTO public.roles (id_role, is_admin, role) VALUES
                    (1, FALSE, 'ROLE_USER'),
                    (2, TRUE, 'ROLE_ADMIN');
                
                RAISE NOTICE 'Roles initialisés avec succès';
            ELSE
                RAISE NOTICE 'Table roles déjà remplie, aucune action';
            END IF;
        ELSE
            RAISE NOTICE 'Table roles introuvable, le backend n''a peut-être pas encore créé le schéma';
        END IF;
    END \$\$;
EOSQL

echo "Script d'initialisation terminé"
