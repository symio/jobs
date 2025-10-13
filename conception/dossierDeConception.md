# Application Jobs

## Tableau de bord de suivi de recherche d'emploi

## 🚀Fonctionnalités
- Authentification JWT sécurisée Oauth2
- Gestion des utilisateurs avec rôles (USER, ADMIN)
- CRUD complet des offres d'emploi
- API REST documentée avec Swagger

## 🛠Technologies
- Backend: Java 21, Spring Boot 3.x, Spring Security 6.x
- Frontend: Angular, TypeScript
- Database: MySQL
- Build: Gradle
- Deployment: WAR sur Tomcat 10 (à venir)
- Security: JWT, OAuth2 Client Credentials

## Architecture de l’application Jobs :
### Diagramme UML de composants :

![Composants Jobs](UML/exports/structureTechnique_componentDiagram.svg)

### Diagramme UML de déploiement : 

![Déploiement Jobs](UML/exports/pileDocker_deploymentDiagram.svg)

### Diagramme UML de Use Case :

![Use Cases Jobs](UML/exports/usecaseDiagram.svg)

### Diagramme UML de Classes :

![Domain CLasses Jobs](UML/exports/classes_classDiagram.svg)

### Diagramme UML de Classes de conception :

![Domain CLasses Jobs](UML/exports/entities_ClassDiagram.svg)

## Modèle logique de données :

![Logic datas model Jobs](UML/exports/logicModelMVP.png)

## Diagramme UML d'activité :
### Action « Se connecter »

![Logic datas model Jobs](UML/exports/login_activityDiagram.svg)

## Diagramme UML de séquence :
### Action « Se connecter »

![Logic datas model Jobs](UML/exports/login_sequenceDiagram.svg)

## Focus sécurité : 
### Architecture Globale de sécurité

![Logic datas model Jobs](UML/exports/archtectureGlobaleSecurite_packageDiagram.svg)

### isolation des données utilisateur (composants)

![Logic datas model Jobs](UML/exports/isolationDonneesUtilisateur_componentDiagram.svg)

### Filtrage sécurisé

#### Filtrer des offres (activité)

![Logic datas model Jobs](UML/exports/filtrageSecurise_activityDiagram.svg)

#### Filtrer des offres (séquence)

![Logic datas model Jobs](UML/exports/rechercheOffresSecurise_sequenceDiagramm.svg)

#### Architecture des repositories et filtrage de sécurité (class diagram)

![Logic datas model Jobs](UML/exports/securedRessourceRepository_classDiagram.svg)
