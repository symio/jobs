#!/bin/bash
set -e

# Génère servers.json avec les vraies valeurs du .env
cat > /pgadmin4/servers.json << EOF
{
  "Servers": {
    "1": {
      "Name": "Jobs PostgreSQL",
      "Group": "Servers",
      "Host": "db",
      "Port": 5432,
      "MaintenanceDB": "${POSTGRES_DB}",
      "Username": "${POSTGRES_USER}",
      "SSLMode": "prefer",
      "PassFile": "/var/lib/pgadmin/pgpass"
    }
  }
}
EOF

# Crée le répertoire si nécessaire
mkdir -p /var/lib/pgadmin

# Génère le fichier pgpass dans le bon emplacement
echo "db:5432:*:${POSTGRES_USER}:${POSTGRES_PASSWORD}" > /var/lib/pgadmin/pgpass
chmod 600 /var/lib/pgadmin/pgpass

# Change le propriétaire pour l'utilisateur pgadmin (UID 5050)
chown -R 5050:5050 /var/lib/pgadmin

# Lance pgAdmin normalement
exec /entrypoint.sh