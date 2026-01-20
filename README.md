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

## Configuration SMTP (Envoi d'emails)

L'application envoie automatiquement les coordonnées de connexion aux nouveaux utilisateurs créés par l'admin.

### Configuration rapide

**Méthode 1 : Variables d'environnement (Recommandé)**

```bash
# Linux/Mac
export SPRING_MAIL_HOST=smtp.gmail.com
export SPRING_MAIL_PORT=587
export SPRING_MAIL_USERNAME=votre_email@gmail.com
export SPRING_MAIL_PASSWORD=votre_mot_de_passe_application

# Windows PowerShell
$env:SPRING_MAIL_HOST="smtp.gmail.com"
$env:SPRING_MAIL_PORT="587"
$env:SPRING_MAIL_USERNAME="votre_email@gmail.com"
$env:SPRING_MAIL_PASSWORD="votre_mot_de_passe_application"
```

**Méthode 2 : Fichier de configuration**

Modifiez `src/main/resources/application-prod.properties` :

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre_email@gmail.com
spring.mail.password=votre_mot_de_passe_application
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Configuration Gmail

Pour Gmail, vous devez :
1. Activer l'authentification à 2 facteurs : https://myaccount.google.com/security
2. Créer un mot de passe d'application : https://myaccount.google.com/apppasswords
3. Utiliser le mot de passe d'application (16 caractères) au lieu de votre mot de passe normal

### Mode Mock (Développement)

En développement, si SMTP n'est pas configuré, les emails sont loggés dans la console. C'est normal et permet de tester sans configurer SMTP.

### Documentation complète

Pour plus de détails sur la configuration SMTP avec différents fournisseurs (Gmail, Outlook, OVH, SendGrid, etc.), consultez le fichier d'exemple : `src/main/resources/application-smtp-example.properties`.

## API REST

Les endpoints REST sont disponibles sous `/api/` et sont **protégés par JWT**.

> 📋 **Note :** Les interfaces React utilisent ces endpoints pour toutes les opérations. Tous les endpoints sont protégés par JWT et nécessitent une authentification.

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
- `POST /api/grades/create` - Créer ou mettre à jour une note
- `GET /api/grades/student/{studentId}` - Notes d'un étudiant
- `GET /api/grades/course/{coursId}` - Notes d'un cours
- `GET /api/grades/student/{studentId}/average` - Moyenne d'un étudiant
- `GET /api/grades/course/{coursId}/success-rate` - Taux de réussite d'un cours

#### Fichiers de Cours (`/api/course-files`)
- `GET /api/course-files/course/{courseId}` - Liste des fichiers d'un cours
- `POST /api/course-files/upload` - Upload un fichier (formateur/admin)
- `GET /api/course-files/{fileId}/download` - Télécharger un fichier
- `GET /api/course-files/{fileId}` - Détails d'un fichier
- `DELETE /api/course-files/{fileId}` - Supprimer un fichier (formateur/admin)

#### Planning (`/api/schedules`)
- `GET /api/schedules` - Liste des séances (optionnellement filtrée par date)
- `GET /api/schedules/{id}` - Détails d'une séance
- `GET /api/schedules/student/{studentId}` - Séances d'un étudiant
- `GET /api/schedules/trainer/{trainerId}` - Séances d'un formateur
- `GET /api/schedules/course/{courseId}` - Séances d'un cours
- `POST /api/schedules` - Créer une séance (formateur/admin)
- `PUT /api/schedules/{id}` - Modifier une séance (formateur/admin)
- `DELETE /api/schedules/{id}` - Supprimer une séance (formateur/admin)
- `POST /api/schedules/{id}/approve` - Approuver une séance (admin)
- `POST /api/schedules/{id}/reject` - Rejeter une séance (admin)

#### Groupes (`/api/groups`)
- `GET /api/groups` - Liste des groupes
- `GET /api/groups/{id}` - Détails d'un groupe
- `GET /api/groups/{id}/students` - Étudiants d'un groupe
- `POST /api/groups` - Créer un groupe (admin)
- `PUT /api/groups/{id}` - Modifier un groupe (admin)
- `DELETE /api/groups/{id}` - Supprimer un groupe (admin)

Pour plus de détails, consultez [`API-DOCUMENTATION.md`](API-DOCUMENTATION.md).

## Interfaces React (Single Page Applications)

Deux applications React sont disponibles pour Formateurs et Étudiants :

### Interface Étudiant (React)
- Accès : `http://localhost:8080/react/etudiant/index.html`
- Authentification JWT
- **Fonctionnalités** :
  - Consultation de mes cours
  - Inscription/désinscription aux cours disponibles
  - Consultation des notes et moyenne générale
  - Emploi du temps par date
- **Technologies** : React 18, Bootstrap 5, API REST

### Interface Formateur (React)
- Accès : `http://localhost:8080/react/formateur/index.html`
- Authentification JWT
- **Fonctionnalités** :
  - Gestion de mes cours
  - Vue des étudiants inscrits par cours
  - Attribution et gestion des notes (avec filtrage des étudiants inscrits)
  - Consultation des notes par cours
  - **Gestion des fichiers** : Upload, téléchargement et suppression de fichiers pour chaque cours
  - **Planification des séances** : Création, modification et suppression de séances planifiées
- **Technologies** : React 18, Bootstrap 5, API REST

### Interface d'Administration (Thymeleaf - SSR)
- Accès : `http://localhost:8080/admin/dashboard`
- Authentification par session
- **Fonctionnalités** :
  - Gestion complète des étudiants, formateurs, cours
  - Gestion des sessions, spécialités, groupes
  - Planification et validation des séances
  - Statistiques et rapports PDF
  - Création de comptes avec envoi automatique des coordonnées par email

### Anciennes interfaces (JavaScript vanilla)
Les anciennes interfaces en JavaScript vanilla sont toujours disponibles :
- Étudiant : `http://localhost:8080/spa/etudiant/index.html`
- Formateur : `http://localhost:8080/spa/formateur/index.html`

> **Note :** Les interfaces Thymeleaf pour étudiants et formateurs ont été supprimées et remplacées par les interfaces React.
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
│   │   ├── dto/            # Data Transfer Objects
│   │   └── GestionFormationApplication.java
│   └── resources/
│       ├── templates/       # Templates Thymeleaf (admin uniquement)
│       ├── static/         # Fichiers statiques
│       │   └── react/      # Interfaces React (étudiant/formateur)
│       └── application*.properties
├── scripts/                # Scripts de sauvegarde SQL
└── test/                   # Tests unitaires
```

## Technologies utilisées

### Backend
- **Spring Boot 3.2.0**
- **Spring Data JPA** avec Hibernate
- **Spring Security** avec JWT
- **Thymeleaf** pour le SSR (interface admin)
- **MySQL** pour la base de données
- **Maven** pour la gestion des dépendances
- **JasperReports** pour la génération de PDF
- **JWT (jjwt)** pour l'authentification API
- **Lombok** pour réduire le boilerplate

### Frontend
- **React 18** (via CDN) pour les interfaces étudiant/formateur
- **Bootstrap 5** pour l'interface
- **Babel** pour la transpilation JSX
- **JavaScript vanilla** pour les anciennes interfaces SPA

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
- ✅ Notifications email automatiques (SMTP configurable)
- ✅ Gestion complète du planning avec détection de conflits
- ✅ Interfaces React complètes pour étudiants et formateurs
- ✅ Authentification JWT pour les API REST
- ✅ Gestion des groupes et spécialités avancée
- ✅ Upload et téléchargement de fichiers pour les cours
- ✅ Planification des séances par les formateurs
- ✅ Création de comptes par l'administrateur uniquement (avec envoi automatique des coordonnées)
- ✅ Scripts de sauvegarde de la base de données (PowerShell et Bash)

## 📚 Guides et Documentation

- **[DOCKER-GUIDE.md](DOCKER-GUIDE.md)** - Guide complet pour Docker (développement et production)
- **[scripts/README-BACKUP.md](scripts/README-BACKUP.md)** - Guide de sauvegarde de la base de données
- **[GUIDES_UTILISATION.md](GUIDES_UTILISATION.md)** - Guide principal avec tous les liens
- **[GUIDE_DEPLOIEMENT_PRODUCTION.md](GUIDE_DEPLOIEMENT_PRODUCTION.md)** - Guide de déploiement en production
- **[TEST_VALIDATION.md](TEST_VALIDATION.md)** - Guide de test des validations
- **[TEST_CORS.md](TEST_CORS.md)** - Guide de test CORS
- **[TEST_CSRF.md](TEST_CSRF.md)** - Guide de test CSRF
- **[ANALYSE_PROJET.md](ANALYSE_PROJET.md)** - Analyse complète du projet
- **[CORRECTIONS_APPLIQUEES.md](CORRECTIONS_APPLIQUEES.md)** - Détails des corrections appliquées

## 💾 Sauvegarde de la Base de Données

Des scripts de sauvegarde sont disponibles dans le dossier `scripts/` :

- **`backup.ps1`** - Script PowerShell pour Windows
- **`backup.sh`** - Script Bash pour Linux/Mac
- **`backup-database.sql`** - Documentation et requêtes SQL

Pour plus de détails, consultez [`scripts/README-BACKUP.md`](scripts/README-BACKUP.md).

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


