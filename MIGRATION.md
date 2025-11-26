# Guide de Migration - GitHub Packages

## Nouveautés de la version

L'application **Jobs** a été refactorisée pour utiliser des librairies Java externalisées, publiées sous forme de packages GitHub. Cette migration permet une meilleure modularité et maintenabilité du code.

## Impact sur les utilisateurs

**Nouvelle exigence** : Vous devez maintenant disposer de :
- Un compte GitHub
- Un Personal Access Token (PAT) avec les droits `read:packages`

Ces identifiants sont nécessaires pour que Gradle puisse télécharger les dépendances depuis le registre GitHub Packages, même si les packages sont publics.

---

## Prérequis : Création du Personal Access Token GitHub

### Étape 1 : Accéder aux paramètres GitHub

1. Connectez-vous à [GitHub](https://github.com)
2. Cliquez sur votre avatar (en haut à droite) puis **Settings**
3. Dans le menu de gauche, tout en bas : **Developer settings**
4. **Personal access tokens** puis **Tokens (classic)**
5. Cliquez sur **Generate new token** puis **Generate new token (classic)**

### Étape 2 : Configurer le token

- **Note** : Donnez un nom explicite (ex: `jobs-packages-read`)
- **Expiration** : Choisissez une durée adaptée (recommandé : 90 jours minimum)
- **Permissions** : Cochez **uniquement** :
  - `read:packages` (Download packages from GitHub Package Registry)

### Étape 3 : Générer et sauvegarder

1. Cliquez sur **Generate token**
2. **IMPORTANT** : Copiez immédiatement le token et conservez-le en lieu sûr
3. Vous ne pourrez plus le voir après avoir quitté cette page

**Format du token** : `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

---

## Nouvelle Installation

Si vous installez l'application pour la première fois, le processus reste similaire :

### 1. Cloner le projet

```bash
git clone git@github.com:symio/jobs.git
cd jobs/conteneurisation
```

### 2. Lancer la configuration automatique

```bash
chmod +x *.sh
./setup-environment.sh
```

Le script vous demandera maintenant :
- Le nom d'instance du projet (par défaut : `jobs`)
- **NOUVEAU** : Votre nom d'utilisateur GitHub
- **NOUVEAU** : Votre Personal Access Token GitHub

### 3. Lancer l'application

```bash
./build-and-run.sh
```

---

## Migration depuis une version antérieure

Si vous avez déjà installé l'application et souhaitez la mettre à jour, suivez ces étapes :

### Option A : Migration automatique (recommandé)

```bash
cd jobs/conteneurisation

# Mettre à jour le code
git pull origin master

# Relancer la configuration (ajoutera les nouvelles variables)
./setup-environment.sh

# Reconstruire et redémarrer
./build-and-run.sh
```

Le script `setup-environment.sh` détectera votre fichier `.env` existant et vous proposera de le reconfigurer pour ajouter les identifiants GitHub.

### Option B : Migration manuelle

Si vous préférez modifier manuellement votre fichier `.env` :

#### 1. Comparer les fichiers

```bash
# Voir les différences entre votre .env et le nouveau modèle
diff .env .env.sample
```

#### 2. Ajouter les nouvelles variables

Ouvrez votre fichier `.env` et ajoutez à la fin :

```bash
# github package access
GITHUB_USERNAME=<votre_nom_utilisateur_github>
GITHUB_TOKEN=<votre_token_github>
```

#### 3. Vérifier les autres changements

Comparez également ces variables qui ont pu évoluer :

```bash
# Nouvelles variables pour le mailer
SPRING_MAIL_AUTH=true
SPRING_MAIL_STARTTLS=true
SPRING_MAIL_HOST=mailer
SPRING_MAIL_PORT=1025
SPRING_MAIL_USER=jobs@loamok.org
SPRING_MAIL_PASS=MailerP@ssw0rd

# Nouvelle variable pour l'URL de base
BASE_URL=''

# Nouvelle variable pour le host de la base de données
DB_HOST=db

# Port backend renommé
BACKEND_PORT=8080  # anciennement PORT=8080
```

#### 4. Reconstruire l'application

```bash
# Arrêter la stack actuelle
docker compose down

# Reconstruire sans cache
docker compose build --no-cache

# Redémarrer
docker compose up -d
```

---

## Résolution des problèmes

### Erreur : "Could not resolve org.loamok.libs:..."

**Cause** : Les identifiants GitHub sont manquants ou incorrects.

**Solution** :
1. Vérifiez que `GITHUB_USERNAME` et `GITHUB_TOKEN` sont bien définis dans `.env`
2. Vérifiez que votre token a bien le droit `read:packages`
3. Vérifiez que le token n'a pas expiré

```bash
# Tester manuellement l'accès au registre
curl -H "Authorization: token VOTRE_TOKEN" \
  https://maven.pkg.github.com/symio/loamok-java-libs
```

### Erreur : "401 Unauthorized" lors du build

**Cause** : Token invalide ou expiré.

**Solution** :
1. Générez un nouveau token sur GitHub
2. Mettez à jour le `.env` avec le nouveau token
3. Relancez `./build-and-run.sh`

### Les identifiants ne sont pas pris en compte

**Cause** : Le fichier `.env` n'est pas rechargé.

**Solution** :
```bash
# Forcer la reconstruction
docker compose down
docker compose build --no-cache
docker compose up -d
```

---

## Vérification de la migration

Pour vérifier que tout fonctionne correctement :

```bash
# Vérifier les logs du backend
docker compose logs backend

# Vous devriez voir des lignes indiquant le téléchargement des packages
# Ex: "Downloading https://maven.pkg.github.com/symio/loamok-java-libs/..."
```

Si le backend démarre sans erreur, la migration est réussie.

---

## Sécurité

**IMPORTANT** : Ne commitez JAMAIS votre fichier `.env` avec vos identifiants GitHub.

Le fichier `.gitignore` est déjà configuré pour ignorer `.env`, mais vérifiez :

```bash
# Vérifier que .env est bien ignoré
git status

# .env ne doit PAS apparaître dans la liste
```

---

## Support

Pour plus d'informations sur l'authentification GitHub Packages :
- [Documentation Gradle](https://docs.github.com/fr/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package)
- [Authentification GitHub Packages](https://docs.github.com/fr/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#authentification-avec-un-personal-access-token)
