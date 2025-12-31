# Application de Gestion d'un Centre de Formation

Application web Spring Boot pour la gestion d'un centre de formation avec support SSR (Thymeleaf) et CSR (API REST).

## Architecture

L'application suit une architecture en couches :

- **Couche Web** : Spring MVC (Controllers Thymeleaf + REST API)
- **Couche Service** : Logique métier
- **Couche Persistance** : Spring Data JPA (Repositories + Entités)
- **Base de données** : MySQL (par défaut, port 3307 pour XAMPP)

## Fonctionnalités

### Gestion des entités principales
- **Étudiant** : matricule, nom, prénom, email, date d'inscription
- **Formateur** : id, nom, spécialité, email
- **Cours** : code, titre, description, formateur, liste d'étudiants inscrits
- **Inscription** : date, étudiant, cours
- **Note** : valeur, étudiant, cours

### Fonctionnalités de base
- CRUD complet pour étudiants, formateurs, cours
- Gestion des inscriptions
- Attribution et consultation des notes
- Gestion des sessions pédagogiques
- Gestion des spécialités et groupes
- Planning et emploi du temps avec détection de conflits
- **Gestion avancée des groupes** : Formateur peut voir tous les groupes avec leurs étudiants
- **Création automatique de spécialités** : Les spécialités des formateurs sont créées automatiquement
- **Création de groupes** : Les étudiants peuvent créer un nouveau groupe lors de l'inscription

### Sécurité
- Authentification via Spring Security
- **JWT (JSON Web Tokens)** pour les API REST
- Rôles : ADMIN, FORMATEUR, ETUDIANT
- Autorisations selon le rôle
- Gestion du profil utilisateur (modification info, changement password)

### Présentation
- **SSR (Server-Side Rendering)** : Interface d'administration en Thymeleaf + Bootstrap
- **CSR (Client-Side Rendering)** : API REST pour client SPA (Angular/React)

## Prérequis

- Java 17 ou supérieur
- Maven 3.6+
- MySQL 8.0+ (pour la production)
- IDE (IntelliJ IDEA, Eclipse, VS Code)

## Installation

1. Cloner le projet
```bash
git clone <repository-url>
cd mini-projet
```

2. Configurer la base de données

**Par défaut (MySQL - XAMPP sur port 3307)** :
- Assurez-vous que MySQL est démarré dans XAMPP
- La base de données `formation_db` sera créée automatiquement
- Lancer l'application :
```bash
mvn spring-boot:run
```

**Pour utiliser un autre profil** :
- **Profil dev** (MySQL sur port 3307, `create-drop`) :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

- **Profil prod** (MySQL sur port 3306) :
  - Modifier `src/main/resources/application-prod.properties` avec vos identifiants
  - Lancer avec le profil prod :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

- **Profil mysql** (MySQL sur port 3307, XAMPP) :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

3. Lancer l'application
```bash
mvn spring-boot:run
```

L'application sera accessible sur `http://localhost:8080`

## Comptes par défaut

### Administrateur
- Username: `admin`
- Password: `admin`

### Étudiant (en mode dev)
- Username: `etudiant1`
- Password: `password`

### Formateur (en mode dev)
- Username: `formateur1`
- Password: `password`

## API REST

Les endpoints REST sont disponibles sous `/api/` et sont **protégés par JWT**.

### Authentification

**POST `/api/auth/login`** - Obtenir un token JWT
```json
{
  "username": "admin",
  "password": "admin"
}
```

Réponse : `{"token": "...", "username": "admin", "roles": ["ROLE_ADMIN"]}`

Utilisez le token dans le header : `Authorization: Bearer <token>`

### Endpoints disponibles

#### Étudiants (`/api/etudiants`)
- `GET /api/etudiants` - Liste des étudiants
- `GET /api/etudiants/{id}` - Détails d'un étudiant
- `POST /api/etudiants` - Créer un étudiant
- `PUT /api/etudiants/{id}` - Modifier un étudiant
- `DELETE /api/etudiants/{id}` - Supprimer un étudiant

#### Cours (`/api/cours`)
- `GET /api/cours` - Liste des cours
- `GET /api/cours/{id}` - Détails d'un cours
- `POST /api/cours` - Créer un cours
- `PUT /api/cours/{id}` - Modifier un cours
- `DELETE /api/cours/{id}` - Supprimer un cours

#### Inscriptions (`/api/inscriptions`)
- `GET /api/inscriptions` - Liste des inscriptions
- `GET /api/inscriptions/{id}` - Détails d'une inscription
- `POST /api/inscriptions` - Créer une inscription (body: `{"studentId": 1, "coursId": 1}`)
- `DELETE /api/inscriptions/student/{studentId}/course/{coursId}` - Supprimer une inscription
- `GET /api/inscriptions/student/{studentId}` - Inscriptions d'un étudiant
- `GET /api/inscriptions/course/{coursId}` - Inscriptions d'un cours

#### Notes (`/api/grades`)
- `GET /api/grades` - Liste des notes
- `GET /api/grades/{id}` - Détails d'une note
- `POST /api/grades` - Attribuer une note
- `PUT /api/grades/{id}` - Modifier une note
- `DELETE /api/grades/{id}` - Supprimer une note

Pour plus de détails, consultez [`API-DOCUMENTATION.md`](API-DOCUMENTATION.md).

## Mini SPA (Single Page Applications)

Deux applications SPA sont disponibles pour Formateurs et Étudiants :

### SPA Formateur
- Accès : `http://localhost:8080/spa/formateur/index.html`
- Authentification JWT
- Gestion des cours et notes
- Vue des étudiants par cours

### SPA Étudiant
- Accès : `http://localhost:8080/spa/etudiant/index.html`
- Authentification JWT
- Consultation des cours, notes et emploi du temps
- Inscription/désinscription aux cours

Pour plus de détails, consultez [`SPA-GUIDE.md`](SPA-GUIDE.md).

## Structure du projet

```
src/
├── main/
│   ├── java/com/formation/
│   │   ├── config/          # Configuration (Security, etc.)
│   │   ├── controller/      # Contrôleurs
│   │   │   ├── admin/       # Contrôleurs Thymeleaf (SSR)
│   │   │   └── api/         # Contrôleurs REST (CSR)
│   │   ├── entity/          # Entités JPA
│   │   ├── repository/      # Repositories Spring Data JPA
│   │   ├── service/         # Services métier
│   │   └── GestionFormationApplication.java
│   └── resources/
│       ├── templates/       # Templates Thymeleaf
│       └── application*.properties
└── test/
```

## Technologies utilisées

- **Spring Boot 3.2.0**
- **Spring Data JPA** avec Hibernate
- **Spring Security** avec JWT
- **Thymeleaf** pour le SSR
- **Bootstrap 5** pour l'interface
- **MySQL** pour la base de données
- **Maven** pour la gestion des dépendances
- **JasperReports** pour la génération de PDF
- **JWT (jjwt)** pour l'authentification API
- **FullCalendar** pour le planning interactif
- **Lombok** pour réduire le boilerplate

## Développement

### Profils Spring Boot

- **Par défaut** : MySQL sur port 3307 (XAMPP), `ddl-auto=update`
- `dev` : MySQL sur port 3307, `ddl-auto=create-drop` (réinitialise la base à chaque démarrage)
- `prod` : MySQL sur port 3306, `ddl-auto=update` (production)
- `mysql` : MySQL sur port 3307 (XAMPP), `ddl-auto=update`

### Tests

```bash
mvn test
```

### Build

```bash
mvn clean package
```

Le fichier JAR sera généré dans `target/gestion-formation-1.0.0.jar`

## Fonctionnalités avancées implémentées

- ✅ Génération de rapports PDF avec JasperReports
- ✅ Statistiques et tableaux de bord
- ✅ Notifications email (service mock disponible)
- ✅ Gestion complète du planning avec détection de conflits
- ✅ Interface étudiant/formateur dédiée
- ✅ FullCalendar pour la visualisation interactive du planning
- ✅ Authentification JWT pour les API REST
- ✅ Gestion des groupes et spécialités avancée (formateur peut voir les groupes, création automatique de spécialités)
- ✅ Upload et téléchargement de fichiers pour les cours

## 📚 Guides et Documentation

- **[GUIDES_UTILISATION.md](GUIDES_UTILISATION.md)** - Guide principal avec tous les liens
- **[GUIDE_DEPLOIEMENT_PRODUCTION.md](GUIDE_DEPLOIEMENT_PRODUCTION.md)** - Guide de déploiement en production
- **[TEST_VALIDATION.md](TEST_VALIDATION.md)** - Guide de test des validations
- **[TEST_CORS.md](TEST_CORS.md)** - Guide de test CORS
- **[TEST_CSRF.md](TEST_CSRF.md)** - Guide de test CSRF
- **[ANALYSE_PROJET.md](ANALYSE_PROJET.md)** - Analyse complète du projet
- **[CORRECTIONS_APPLIQUEES.md](CORRECTIONS_APPLIQUEES.md)** - Détails des corrections appliquées

## 🔐 Configuration des Variables d'Environnement

Pour la production, configurez les variables d'environnement :

```bash
# Copier le fichier d'exemple
cp .env.example .env

# Éditer avec vos valeurs réelles
# Voir GUIDE_DEPLOIEMENT_PRODUCTION.md pour les détails
```

Variables importantes :
- `JWT_SECRET` - Secret pour les tokens JWT
- `SPRING_MAIL_USERNAME` / `SPRING_MAIL_PASSWORD` - Configuration email
- `CORS_ALLOWED_ORIGINS` - Domaines autorisés pour CORS

## Auteur

Projet réalisé dans le cadre du cours d'Architectures Logicielles Evoluées - Framework Spring

## Licence

Ce projet est à des fins éducatives.


