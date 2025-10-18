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
      "MaintenanceDB": "postgres",
      "Username": "${POSTGRES_USER}",
      "SSLMode": "prefer",
      "PassFile": "/pgpass"
    }
  }
}
EOF

# Génère le fichier pgpass avec les credentials du .env
echo "db:5432:*:${POSTGRES_USER}:${POSTGRES_PASSWORD}" > /pgpass
chmod 600 /pgpass

# Lance pgAdmin normalement
/entrypoint.sh