#!/bin/bash
set -e

echo "Attente que le backend soit prêt..."
sleep 10

echo "Vérification de l'existence de la table roles..."

# Vérifie si la table existe et est vide avant d'insérer
PGPASSWORD=$POSTGRES_PASSWORD psql -h ${POSTGRES_HOST} -U $POSTGRES_USER -d $POSTGRES_DB <<-EOSQL
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

echo "Mise à jour des Jobs sans date de candidature ..."
PGPASSWORD=$POSTGRES_PASSWORD psql -h ${POSTGRES_HOST} -U $POSTGRES_USER -d $POSTGRES_DB <<-EOSQL
    DO \$\$
    BEGIN
        -- Vérifie si la table jobs existe
        IF EXISTS (
            SELECT FROM information_schema.tables 
            WHERE table_schema = 'public'
            AND table_name = 'jobs'
        ) THEN
            -- Vérifie si les colonnes application_date et created_at existent
            IF EXISTS (
                SELECT FROM information_schema.columns 
                WHERE table_schema = 'public'
                AND table_name = 'jobs'
                AND column_name = 'application_date'
            )
            AND EXISTS (
                SELECT FROM information_schema.columns 
                WHERE table_schema = 'public'
                AND table_name = 'jobs'
                AND column_name = 'created_at'
            ) THEN

                -- Vérifie s'il y a des lignes à mettre à jour
                IF EXISTS (
                    SELECT 1 FROM public.jobs WHERE application_date IS NULL
                ) THEN
                    UPDATE public.jobs
                    SET application_date = created_at
                    WHERE application_date IS NULL;

                    RAISE NOTICE 'Champs application_date mis à jour avec created_at pour les lignes NULL.';
                ELSE
                    RAISE NOTICE 'Aucune ligne à mettre à jour dans public.jobs.';
                END IF;

            ELSE
                RAISE NOTICE 'Les colonnes application_date et/ou created_at sont absentes de la table jobs.';
            END IF;
        ELSE
            RAISE NOTICE 'Table jobs introuvable, le schéma n''est peut-être pas encore créé.';
        END IF;
    END \$\$;
EOSQL

echo "Script d'initialisation terminé"
