
# Jobs
## Tableau de bord de suivi de recherche d'emploi

## 🚀 Fonctionnalités
- Authentification JWT sécurisée Oauth2
- Gestion des utilisateurs avec rôles (USER, ADMIN)
- CRUD complet des offres d'emploi
- API REST documentée avec Swagger

## 🛠️ Technologies
- Backend: Java 21, Spring Boot 3.x, Spring Security 6.x
- Frontend: Angular, TypeScript
- Database: MySQL
- Build: Gradle
- Deployment: WAR sur Tomcat 10 (à venir)
- Security: JWT, OAuth2 Client Credentials

## 📦 Installation

### Prérequis
- JDK 21+
- Node.js 22+ et yarn 1.22
- PostgreSQL 17+
- Apache Tomcat 10+

### Configuration
**Configuration Spring :**
```properties
# application.yml
spring:
  profiles.active: secrets
  datasource:
    url: jdbc:postgresql://localhost:5432/jobs?currentSchema=public
# application-secrets.sample.yml
app.jwt.secret: NotARealJwtSecret
spring:
  datasource:
    username: AdminUser
    password: AdminPassword
```

**Configuration Base de données :**
- Définition des variables via le fichier conteneurisation/.env (non versionné Git)

```bash
# .env.sample
# Postgresql Environment Variables
POSTGRES_DB=dbname
POSTGRES_USER=dbuser
POSTGRES_PASSWORD=dbuserp@ssw0rd
# pgAdmin Environment Variables
PGADMIN_DEFAULT_EMAIL=dummyemail@dummy.org # Dummy email but correctly form
PGADMIN_DEFAULT_PASSWORD=d3fault_p@ssw0rd
PGADMIN_LISTEN_ADDRESS=[::] # Default value
PPGADMIN_LISTEN_PORT=80 # Default value when TLS disabled
```

### Angular 

Se placer dans le dossier j-jobs_frontend
Lancer la commande : 
``` bash
yarn install
```

## Installation 
- Cloner ce dépôt
- Configurez les fichiers d’environnement / de secrets
> - copier le fichier : j-jobs_backend/src/main/resources/application-secrets.sample.yml vers j-jobs_backend/src/main/resources/application-secrets.yml
>  - adaptez les variables username et  password
>  - copiez le fichier -   conteneurisation/.env.sample vers conteneurisation/.env
>  - adaptez les variables en accord avec j-jobs_backend/src/main/resources/application-secrets.yml
- Lancez la pile docker : `` docker compose up --build -d --wait ``
- Importez le fichier :  j-jobs_backend/src/main/resources/data-roles.csv via PGAdmin 4 servi par Docker (login et mot de passe sont définis dans le .env) : http://localhost:5433/browser/
>  Vous devrez ajouter un "serveur" dans PGAdmin avec : 
> - Hôte : db
> - Port : 5432
> - identifiant : le POSTGRES_USER mis dans le .env
> - Mot de passe : le POSTGRES_PASSWORD mis dans le .env
> connectez vous à la base de données jobs (ou le nom que vous aurez mis en POSTGRES_DB dans le .env) depuis l'explorateur d'objets
> cliquez droit sur Schémas > public > Tables > roles et suivez les instruction de l'assistant "Import / Export de données"

Ouvrez votre navigateur sur l'url : http://localhost:4200

---

## 📝Notes de Licence

### 1. Documents et éléments graphiques

Tous les documents, images, diagrammes et, de manière générale, **toutes les illustrations créées pour l’application _Jobs_** sont couverts par la licence décrite dans le présent chapitre.  
**Exception :** les logos, marques et noms de logiciels cités demeurent la propriété exclusive de leurs détenteurs respectifs.

#### 📄 Licence applicable
L’ensemble de ces éléments est diffusé sous licence **Creative Commons Attribution - Partage dans les Mêmes Conditions 4.0 International (CC BY-SA 4.0)**.  
🔗 Licence complète : [https://creativecommons.org/licenses/by-sa/4.0/deed.fr](https://creativecommons.org/licenses/by-sa/4.0/deed.fr)

#### ✳️ Résumé de la licence
La **CC BY-SA 4.0** autorise toute personne à :

- **Partager** : copier, reproduire, distribuer et communiquer l’œuvre ;  
- **Adapter** : remixer, transformer et créer à partir de l’œuvre,  
- **Y compris à des fins commerciales**,  
- **Sur tout support et par tout moyen**.

#### ⚖️ Obligations associées
Toute utilisation de l’œuvre implique de :

- 🏷️ **Attribuer** l’œuvre à ses auteurs originaux, indiquer la source et signaler les éventuelles modifications ;  
- 🔁 **Partager dans les mêmes conditions** : toute création dérivée doit être diffusée sous la même licence **CC BY-SA 4.0**, afin de garantir la libre réutilisation et modification.

---

### 2. Code source

Le **code source** des éléments logiciels constituant l’application _Jobs_ est placé sous licence **Open Source GNU General Public License version 3 (GPLv3)**.

#### 💻 Résumé de la licence
La **GPLv3** garantit à tout utilisateur le droit de :

- Exécuter, étudier et modifier le code source ;  
- Redistribuer le code original ou modifié ;  
- À condition que toute redistribution soit effectuée sous la même licence **GPLv3**, afin de préserver le caractère libre du logiciel.

🔗 Licence complète : [https://www.gnu.org/licenses/gpl-3.0.html](https://www.gnu.org/licenses/gpl-3.0.html)

---

🧩 _Ce double régime de licences permet de distinguer clairement les droits applicables aux éléments graphiques (CC BY-SA 4.0) et au code source (GPLv3), tout en assurant une compatibilité et une cohérence avec les principes du logiciel libre._
