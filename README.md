
# Jobs
## Tableau de bord de suivi de recherche d'emploi

## Système d'authentification JWT avec OAuth2
L'application "Jobs" implémente un système d'authentification basé sur JWT (JSON Web Tokens) suivant le protocole OAuth2 Client Credentials dont le flux de sécurité à été renforcé.

- Architecture d'authentification avec génération et validation de tokens
- Service JwtService.java pour la gestion complète du cycle de vie des tokens (génération, signature HMAC-SHA, validation, extraction des claims)
- Implémentation de méthodes spécifiques : `generateClientCredentialsToken()` pour créer des tokens sécurisés avec durée de validité configurable, `isClientCredentialsTokenValid()` pour valider les tokens reçus
- Gestion des cas d'erreur : tokens expirés, signature invalide, format incorrect
- Méthode `extractAllClaimsForced()` permettant de récupérer les claims même d'un token expiré (nécessaire pour le mécanisme de refresh token)
- Tests unitaires et d'intégration pour garantir la sécurité du système

### Informations complémentaires

Ce système d'authentification constitue le cœur de la sécurité de l'application. Il as été pensé pour optimiser la robustesse du système en implémentant :
- Une validation stricte incluant la vérification de la signature, de l'expiration et du type de token
- Un système de scopes OAuth2 (access, admin) pour une gestion granulaire des permissions
- Des logs détaillés pour le monitoring et l'audit des tentatives d'authentification

Le code source complet est disponible dans les fichiers JwtService.java et est utilisé par l'ensemble de l'application pour sécuriser tous les endpoints de l'API REST.

---

## Configuration de Spring Security avec contrôle d'accès basé sur les rôles (RBAC)

L'application Jobs implémente une politique de sécurité complète pour l'API REST, dont une partie est gérée par Spring Data Rest, en définissant précisément les autorisations d'accès à chaque endpoint selon les rôles utilisateurs.

- Définition de la matrice des droits d'accès selon 2 rôles principaux (USER, ADMIN)
- Configuration de SecurityFilterChain dans SecurityConfig.java avec règles d'autorisation granulaires
- Endpoints publics sans authentification (inscription, obtention de token, rafraîchissement de token, retour d'authentification (RememberMe))
- Endpoints personnalisés et auto générés protégés par rôle : administration des rôles réservée aux ADMIN, accès mixte USER/ADMIN pour la consultation et modification des données
- Double couche de sécurité : vérification du rôle ET du scope OAuth2
- Méthode personnalisée `hasAccessScopeAndAuthenticated()` ajoutant une validation supplémentaire des scopes
- Configuration CORS sécurisée avec whitelist stricte des origines autorisées

**Configuration appliquée :**
```java
// Endpoints publics
.requestMatchers(HttpMethod.POST, "/profil/register").permitAll()
.requestMatchers(HttpMethod.POST, "/authorize/token").permitAll()

// Endpoints admin uniquement
.requestMatchers(HttpMethod.GET, "/roles/**").hasRole("ADMIN")
.requestMatchers(HttpMethod.POST, "/roles").hasRole("ADMIN")

// Endpoints mixtes
.requestMatchers(HttpMethod.GET, "/**").hasAnyRole("USER", "ADMIN")
```

### Informations complémentaires

Cette configuration reflète les bonnes pratiques de sécurisation d'API REST modernes :
- Principe du moindre privilège : chaque utilisateur n'a accès qu'aux ressources nécessaires à son rôle
- Défense en profondeur : combinaison de rôles Spring Security et scopes OAuth2
- CORS strictement configuré (pas de wildcard "*") pour éviter les requêtes malveillantes
- Architecture stateless facilitant la scalabilité horizontale de l'application

La configuration CORS autorise uniquement les origines localhost:4200 (développement Angular) et l'IP du réseau local, ce qui limite l'exposition de l'API.

---

## Filtre d'authentification personnalisé avec validation de signature client

Pour renforcer la sécurité au-delà du simple JWT, l'application Jobs utilise un filtre Spring Security personnalisé (JwtAuthenticationFilter) qui intercepte chaque requête HTTP pour valider l'authentification de manière approfondie.

- Classe JwtAuthenticationFilter héritant de OncePerRequestFilter
- Implémentation de la méthode `doFilterInternal()` pour traiter chaque requête
- Extraction et validation du token JWT depuis l'en-tête Authorization (format Bearer)
- Mécanisme innovant de "signature client" pour empêcher la réutilisation de tokens volés
- Génération d'une empreinte unique basée sur User-Agent et d'autres variables provenant du client (acceptLang, secChUa, secChUaPlatform, .. etc, (la liste complète varie entre la version publique et privée de l'application)) via ClientSignatureBuilder
- Validation croisée : le token JWT doit être valide ET la signature client doit correspondre à celle stockée en base
- Construction du contexte de sécurité Spring avec les autorités (rôles + scopes) extraites du token
- Gestion des erreurs avec logging détaillé sans exposition d'informations sensibles
- Injection du contexte d'authentification dans SecurityContextHolder pour autorisation des contrôleurs

**Pattern et architecture :**
- Pattern Chain of Responsibility (Filter Chain) de Spring Security
- Injection de dépendances pour JwtService, UserRepository, ClientSignatureBuilder
- OncePerRequestFilter garantissant une seule exécution par requête

**Sécurité avancée :**
- Double validation : token JWT classique + signature client unique
- Vérification que le token stocké en base (authToken) est également valide
- Comparaison de l'empreinte client pour détecter un vol de token

**Code clé :**
```java
String clientSignature = csb.buildClientSignature(request);
String authTokenSignature = jwtService.extractAllClaims(user.getAuthToken())
    .get("client-signature", String.class);

if (authTokenSignature != null && authTokenSignature.equals(clientSignature)) {
    // Token valide pour ce client spécifique
    Collection<GrantedAuthority> authorities = Arrays.stream(scopes.split(" "))
        .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
        .collect(Collectors.toList());
}
```
### Informations complémentaires

Ce filtre ajoute une couche de sécurité significative au-delà des standards JWT habituels. Points clés :

**Innovation - Signature client :**
Un attaquant qui intercepterait un token JWT valide ne pourrait pas l'utiliser depuis un autre appareil/navigateur, car la signature client (basée sur User-Agent + d'autres facteurs dont certains restent secret pour garantir la sécurité) ne correspondrait pas. C'est une protection supplémentaire contre les attaques man-in-the-middle ou le vol de tokens.

**Performance :**
Le filtre est optimisé pour ne pas impacter les performances : validation en mémoire, arrêt anticipé si le token est invalide, et continuation rapide de la chaîne de filtres pour les requêtes légitimes.

**Logging sécurisé :**
Les erreurs sont loggées côté serveur pour audit, mais les messages retournés au client sont génériques pour ne pas révéler d'informations sur la structure de sécurité.

---

## Architecture Backend en couches (Controller - Service - Repository)

L'architecture backend de l'application Jobs est développée selon le pattern multicouche, garantissant séparation des responsabilités, maintenabilité et testabilité.

- **Couche Controller :** Création des endpoints REST avec annotations Spring (@RestController, @GetMapping, @PostMapping), gestion des requêtes HTTP, validation des entrées utilisateur, sérialisation JSON des réponses
- **Couche Service :** Implémentation de la logique métier (AuthorizationService, UserService), orchestration des appels aux repositories, gestion des transactions avec @Transactional, validation des règles métier
- **Couche Repository :** Interfaces JPA étendant JpaRepository pour l'accès aux données, requêtes JPQL personnalisées si nécessaire
- **Couche Sécurité :** Package dédié (org.loamok.jobs.security) avec SecurityConfig, JwtService, filtres d'authentification
- Définition des DTOs (Data Transfer Objects) pour isoler le modèle de données des API exposées
- Gestion centralisée des exceptions avec @ControllerAdvice pour des retours d'erreur cohérents

**Framework et patterns :**
- Spring Boot pour l'injection de dépendances et l'inversion de contrôle
- Pattern MVC (Model-View-Controller) adapté pour API REST
- Pattern Repository pour l'abstraction de l'accès aux données
- JPA/Hibernate pour l'ORM (Object-Relational Mapping)

**Structure des packages :**
```
org.loamok.jobs/
├── controller/     # Endpoints REST
├── service/        # Logique métier
├── repository/     # Accès données
├── entity/         # Entités JPA
├── security/       # Configuration sécurité
├── dto/            # Objets de transfert
└── util/           # Classes utilitaires
```

### Informations complémentaires

**Avantages de cette architecture :**
- **Testabilité :** Chaque couche peut être testée indépendamment (tests unitaires pour services, tests d'intégration pour repositories)
- **Maintenabilité :** Modification d'une couche sans impact sur les autres (ex: changer de base de données n'impacte que la couche Repository)
- **Réutilisabilité :** Les services métier peuvent être appelés depuis différents contrôleurs
- **Sécurité :** La couche Security est isolée et appliquée de manière transversale via les filtres Spring

**Exemple concret :**
Un endpoint `/users/{id}` passe par :
1. UserController : reçoit la requête HTTP, valide le paramètre
2. UserService : vérifie les permissions, applique la logique métier
3. UserRepository : récupère les données en base via JPA
4. Retour inverse avec transformation Entity → DTO → JSON

---


**Gestion des tokens :**
Le token JWT "Serveur" et le refresh token "Serveur" sont stockés dans l'entité User pour validation à chaque requête. Cette approche permet de révoquer un token en le supprimant de la base (déconnexion forcée).

**Evolution :**
Le modèle a été pensé pour être facilement extensible (ajout de nouvelles entités, relations) sans refonte majeure.

---

### Frontend Angular avec architecture modulaire

La partie frontend de l'application Jobs est réalisé avec Angular en appliquant une architecture modulaire et les bonnes pratiques du framework.

- Structuration en modules fonctionnels
- Création de composants "standalone" avec cycle de vie Angular (ngOnInit, ngOnDestroy)
- Développement de services Angular pour la communication avec l'API REST (@Injectable)
- Implémentation d'un service d'authentification (AuthService) gérant les tokens JWT
- Création d'un HTTP Interceptor pour ajouter automatiquement le token Bearer aux requêtes et déconnecter un utilisateur (suppression des tokens côtés serveur et client) à expiration du token "Serveur"
- Développement d'un Guard d'authentification (AuthGuard) pour protéger les routes
- Mise en place du routing avec lazy-loading des modules pour optimiser les performances
- Création de formulaires réactifs avec validation (Reactive Forms)
- Gestion des erreurs HTTP avec notification utilisateur (toaster, messages d'erreur)
- Styling avec CSS/SCSS entièrement Responsive via CSS Flexbox
- respect des principales règles d'accessibilité (contraste des couleurs, ..)

**Framework et outils :**
- Angular (version récente) avec TypeScript
- Angular CLI pour la génération de code et le build
- RxJS pour la programmation réactive (Observables)
- HttpClient pour les appels API
- Angular Router pour la navigation

**Architecture Angular :**
```
src/app/
├── services/              # Services singleton (AuthService)
├── components/            # Composants réutilisables
├── pages/								 # Modules fonctionnels
│   ├── dashboard/         # Module Tableau de bord
│   │   ├── job-details/   # Module Détails d'une offre
│   │   ├── job-form/      # Module Forumulaire d'une offre
│   ├── login/             # Module identification utilisateur
│   ├── register/          # Module auto-enregistrement utilisateurs
│   └── landing-page/      # Module "page d'accueil" de l'application
├── interceptors/          # Http Interceptors
├── theme/                 # feuilles de style commuunes
└── guards/                # Route guards
```

**Patterns utilisés :**
- Injection de dépendances Angular
- Observables pour la gestion asynchrone
- Interceptors pour la logique transversale (tokens)
- Guards pour la protection de routes

### Informations complémentaires

**Sécurité côté client :**
- Stockage sécurisé du token 
- Déconnexion automatique à l'expiration du token
- Nettoyage du token au logout
- Redirection vers login si 401 Unauthorized

**Communication Backend/Frontend :**
- Configuration CORS validée entre Angular (localhost:4200) et API Spring (localhost:8080) (en développement)
- Gestion des erreurs HTTP avec retry automatique pour les erreurs réseau temporaires
- Affichage de messages d'erreur clairs pour l'utilisateur

**Optimisations :**
- Lazy-loading pour ne charger que les modules nécessaires
- Utilisation de OnPush change detection pour améliorer les performances
- Unsubscribe des Observables pour éviter les fuites mémoire

---

## Configuration spring :

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

**À faire avant mise en prod pour une sécurité renforcée :**
- Désactivation de spring.jpa.show-sql=false en production
- Configuration server.error.include-stacktrace=never
- Limitation des origines CORS aux domaines de production uniquement
- Configuration de headers de sécurité (X-Frame-Options, X-Content-Type-Options)

### Informations complémentaires

**Checklist de sécurité pour la production :**
- Secrets externalisés (jamais dans le code)
- HTTPS obligatoire
- CORS configuré strictement
- Logs de sécurité activés
- Stack traces masquées pour les erreurs
- Rate limiting sur les endpoints sensibles (à implémenter)
- Monitoring et alertes configurés

**Gestion des mises à jour :**
Plan de déploiement incluant :
- Sauvegarde de la base de données avant migration
- Exécution des scripts de migration Flyway/Liquibase
- Tests de smoke en production
- Rollback plan en cas de problème

---

## Conteneurisation avec Docker et Docker Compose

L'application Jobs est prévue pour être conteneurisée avec Docker pour faciliter le déploiement multi-environnements et garantir la reproductibilité.

**Tâches encore à réaliser :**
- Création d'un Dockerfile multi-stage pour optimiser la taille de l'image :
  - Stage 1 : Build avec Maven (compilation du .jar / war)
  - Stage 2 : Runtime avec JRE léger (OpenJDK alpine)
- Configuration d'un docker-compose.yml orchestrant tous les services :
  - Service backend (API Spring Boot)
  - Service frontend (Angular avec nginx)
  - Service base de données (PostgreSQL)
  - Réseau Docker interne pour la communication inter-services
- Définition des volumes Docker pour la persistance des données (BDD)
- Configuration des healthchecks pour vérifier l'état des conteneurs
- Paramétrage des variables d'environnement via fichier .env
- Création d'un .dockerignore pour optimiser le contexte de build
- Tests de l'ensemble de la stack avec `docker-compose up`
- Documentation du déploiement Docker dans le README

