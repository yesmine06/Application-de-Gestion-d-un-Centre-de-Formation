# Diagrammes UML pour le Rapport JEE

## 1. Diagramme de Cas d'Utilisation

```
┌─────────────────────────────────────────────────────────────────┐
│                        SYSTÈME                                   │
│                   Gestion de Formation                           │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
   ┌─────────┐          ┌──────────┐          ┌─────────┐
   │ Admin   │          │Formateur │          │Étudiant │
   └────┬────┘          └────┬─────┘          └────┬────┘
        │                    │                     │
        │                    │                     │
   ┌────┴─────────────────────┴─────────────────────┴────┐
   │                                                      │
   │  ┌──────────────────────────────────────────────┐   │
   │  │ Authentification                              │   │
   │  └──────────────────────────────────────────────┘   │
   │                                                      │
   │  ┌──────────────────────────────────────────────┐   │
   │  │ ADMIN                                        │   │
   │  ├──────────────────────────────────────────────┤   │
   │  │ • Gérer les étudiants                        │   │
   │  │ • Gérer les formateurs                       │   │
   │  │ • Créer des comptes (avec envoi email)      │   │
   │  │ • Gérer les cours                            │   │
   │  │ • Gérer les sessions                         │   │
   │  │ • Gérer les groupes                          │   │
   │  │ • Gérer les spécialités                      │   │
   │  │ • Assigner des cours aux formateurs         │   │
   │  └──────────────────────────────────────────────┘   │
   │                                                      │
   │  ┌──────────────────────────────────────────────┐   │
   │  │ FORMATEUR                                    │   │
   │  ├──────────────────────────────────────────────┤   │
   │  │ • Créer des cours                            │   │
   │  │ • Attribuer des notes                        │   │
   │  │ • Visualiser les notes par cours             │   │
   │  │ • Gérer les fichiers de cours                │   │
   │  │   - Upload de fichiers                       │   │
   │  │   - Télécharger des fichiers                 │   │
   │  │   - Supprimer des fichiers                   │   │
   │  │ • Planifier des séances                      │   │
   │  │   - Créer une séance                         │   │
   │  │   - Modifier une séance                      │   │
   │  │   - Supprimer une séance                     │   │
   │  │ • Visualiser les étudiants par cours        │   │
   │  └──────────────────────────────────────────────┘   │
   │                                                      │
   │  ┌──────────────────────────────────────────────┐   │
   │  │ ÉTUDIANT                                     │   │
   │  ├──────────────────────────────────────────────┤   │
   │  │ • Consulter les cours disponibles            │   │
   │  │ • S'inscrire à un cours                      │   │
   │  │ • Se désinscrire d'un cours                  │   │
   │  │ • Consulter ses notes                        │   │
   │  │ • Consulter sa moyenne générale              │   │
   │  │ • Consulter l'emploi du temps                │   │
   │  │   - Calendrier mensuel                       │   │
   │  │   - Filtrage par cours                       │   │
   │  │   - Détails des séances                      │   │
   │  │ • Télécharger les fichiers de cours          │   │
   │  └──────────────────────────────────────────────┘   │
   │                                                      │
   └──────────────────────────────────────────────────────┘
```

## 2. Diagramme de Classes (Simplifié)

```
┌─────────────────────────────────────────────────────────────┐
│                         User                                 │
│  (Abstract, Inheritance Strategy: JOINED)                   │
├─────────────────────────────────────────────────────────────┤
│ - id: Long                                                   │
│ - username: String                                           │
│ - password: String                                           │
│ - email: String                                              │
│ - nom: String                                                │
│ - prenom: String                                             │
│ - enabled: Boolean                                           │
│ - dateCreation: LocalDateTime                                │
│ - roles: Set<Role>                                           │
└───────────────┬─────────────────────────────────────────────┘
                │
        ┌───────┴───────┐
        │               │
        ▼               ▼
┌───────────────┐  ┌───────────────┐
│   Student     │  │   Trainer     │
├───────────────┤  ├───────────────┤
│ - matricule   │  │ - specialite  │
│ - dateInscrip │  └───────────────┘
│ - group       │
│ - specialty   │
└───────┬───────┘
        │
        │ (1)
        │
        │ (N)
        ▼
┌───────────────┐
│  Enrollment   │
├───────────────┤
│ - id          │
│ - student     │──────┐
│ - cours       │──────┤
│ - dateInscrip │      │
└───────────────┘      │
                       │
┌───────────────┐      │
│    Grade      │      │
├───────────────┤      │
│ - id          │      │
│ - student     │──────┘
│ - cours       │──────┐
│ - valeur      │      │
│ - commentaire │      │
│ - dateAttrib  │      │
└───────────────┘      │
                       │
┌───────────────┐      │
│    Course     │      │
├───────────────┤      │
│ - id          │      │
│ - code        │      │
│ - titre       │      │
│ - description │      │
│ - formateur   │──────┘
│ - session     │──────┐
└───────────────┘      │
                       │
┌───────────────┐      │
│   Schedule    │      │
├───────────────┤      │
│ - id          │      │
│ - cours       │──────┘
│ - date        │
│ - heureDebut  │
│ - heureFin    │
│ - salle       │
│ - status      │
└───────────────┘

┌───────────────┐
│   Session     │
├───────────────┤
│ - id          │
│ - nom         │
│ - type        │
│ - dateDebut   │
│ - dateFin     │
└───────────────┘

┌───────────────┐
│ CourseFile    │
├───────────────┤
│ - id          │
│ - cours       │
│ - nomFichier  │
│ - typeMime    │
│ - taille      │
│ - chemin      │
│ - dateUpload  │
└───────────────┘

┌───────────────┐
│    Group      │
├───────────────┤
│ - id          │
│ - nom         │
│ - description │
└───────────────┘

┌───────────────┐
│  Specialty    │
├───────────────┤
│ - id          │
│ - nom         │
│ - description │
└───────────────┘

┌───────────────┐
│     Role      │
├───────────────┤
│ - id          │
│ - name        │ (ADMIN, FORMATEUR, ETUDIANT)
└───────────────┘
```

## 3. Diagramme de Séquence - Authentification (JWT)

```
┌─────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ Client  │    │ React App   │    │AuthController│    │UserService  │    │   Database   │
│(Browser) │    │             │    │              │    │             │    │              │
└────┬────┘    └──────┬──────┘    └──────┬───────┘    └──────┬──────┘    └──────┬───────┘
     │                 │                  │                   │                  │
     │ 1. Login Form   │                 │                   │                  │
     │────────────────>│                 │                   │                  │
     │                 │                 │                   │                  │
     │                 │ 2. POST /api/auth/login            │                  │
     │                 │    {username, password}             │                  │
     │                 │────────────────>│                  │                  │
     │                 │                 │                   │                  │
     │                 │                 │ 3. findByUsername │                  │
     │                 │                 │──────────────────>│                  │
     │                 │                 │                   │                  │
     │                 │                 │                   │ 4. SELECT user   │
     │                 │                 │                   │─────────────────>│
     │                 │                 │                   │                  │
     │                 │                 │                   │ 5. User data    │
     │                 │                 │                   │<─────────────────│
     │                 │                 │                   │                  │
     │                 │                 │ 6. Validate password                │
     │                 │                 │<──────────────────│                  │
     │                 │                 │                   │                  │
     │                 │                 │ 7. Generate JWT    │                  │
     │                 │                 │───────────────────│                  │
     │                 │                 │                   │                  │
     │                 │ 8. Response    │                   │                  │
     │                 │    {token, userInfo}               │                  │
     │                 │<────────────────│                  │                  │
     │                 │                 │                   │                  │
     │ 9. Store token  │                 │                   │                  │
     │    in localStorage                │                   │                  │
     │<────────────────│                 │                   │                  │
     │                 │                 │                   │                  │
     │ 10. Subsequent requests           │                   │                  │
     │     Authorization: Bearer <token>  │                   │                  │
     │────────────────>│                 │                   │                  │
     │                 │                 │                   │                  │
```

## 4. Diagramme de Séquence - Inscription à un Cours (Étudiant)

```
┌─────────┐    ┌──────────────┐    ┌──────────────────┐    ┌──────────────┐    ┌──────────────┐
│Étudiant │    │ React App    │    │Enrollment        │    │Enrollment    │    │   Database   │
│         │    │              │    │RestController    │    │Service       │    │              │
└────┬────┘    └──────┬───────┘    └────────┬─────────┘    └──────┬───────┘    └──────┬───────┘
     │                 │                    │                     │                   │
     │ 1. Click        │                    │                     │                   │
     │    "S'inscrire" │                    │                     │                   │
     │────────────────>│                    │                     │                   │
     │                 │                    │                     │                   │
     │                 │ 2. Disable button │                    │                     │                   │
     │                 │───────────────────│                    │                     │                   │
     │                 │                    │                     │                   │
     │                 │ 3. POST /api/inscriptions               │                   │
     │                 │    {studentId, coursId}                  │                   │
     │                 │──────────────────>│                     │                   │
     │                 │                    │                     │                   │
     │                 │                    │ 4. enroll(studentId, coursId)          │
     │                 │                    │─────────────────────>│                 │
     │                 │                    │                     │                   │
     │                 │                    │                     │ 5. Check if exists│
     │                 │                    │                     │───────────────────>│
     │                 │                    │                     │                   │
     │                 │                    │                     │ 6. Result         │
     │                 │                    │                     │<──────────────────│
     │                 │                    │                     │                   │
     │                 │                    │                     │ 7. Create enrollment│
     │                 │                    │                     │───────────────────>│
     │                 │                    │                     │                   │
     │                 │                    │                     │ 8. Enrollment saved│
     │                 │                    │                     │<──────────────────│
     │                 │                    │                     │                   │
     │                 │                    │ 9. EnrollmentDto    │                   │
     │                 │                    │<────────────────────│                  │
     │                 │                    │                     │                   │
     │                 │ 10. Success        │                     │                   │
     │                 │     Response       │                     │                   │
     │                 │<───────────────────│                    │                     │                   │
     │                 │                    │                     │                   │
     │                 │ 11. Refresh data   │                    │                     │                   │
     │                 │     - MyCourses    │                    │                     │                   │
     │                 │     - Available    │                    │                     │                   │
     │                 │───────────────────│                    │                     │                   │
     │                 │                    │                     │                   │
     │ 12. UI Updated  │                    │                     │                   │
     │     - Course moved to "Mes Cours"    │                     │                   │
     │     - Removed from "Disponibles"      │                     │                   │
     │<────────────────│                    │                     │                   │
     │                 │                    │                     │                   │
```

## 5. Diagramme de Séquence - Création de Compte par Admin

```
┌─────────┐    ┌──────────────┐    ┌──────────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ Admin   │    │ Thymeleaf    │    │AdminStudent     │    │Registration │    │Password     │    │ Email       │
│         │    │ Template     │    │Controller       │    │Service      │    │Service      │    │Service      │
└────┬────┘    └──────┬───────┘    └────────┬────────┘    └──────┬───────┘    └──────┬───────┘    └──────┬───────┘
     │                 │                    │                     │                   │                   │
     │ 1. Fill form    │                    │                     │                   │                   │
     │    (nom, prenom,│                    │                     │                   │                   │
     │     email, etc.)│                    │                     │                   │                   │
     │────────────────>│                    │                     │                   │                   │
     │                 │                    │                     │                   │                   │
     │                 │ 2. POST /admin/students                  │                   │                   │
     │                 │    RegistrationDto │                    │                     │                   │
     │                 │───────────────────>│                    │                     │                   │
     │                 │                    │                     │                   │                   │
     │                 │                    │ 3. createUserByAdmin │                   │                   │
     │                 │                    │─────────────────────>│                  │                   │
     │                 │                    │                     │                   │                   │
     │                 │                    │                     │ 4. generateRandomPassword│            │
     │                 │                    │                     │───────────────────>│                  │
     │                 │                    │                     │                   │                   │
     │                 │                    │                     │ 5. Random password │                  │
     │                 │                    │                     │<───────────────────│                  │
     │                 │                    │                     │                   │                   │
     │                 │                    │                     │ 6. encode password │                  │
     │                 │                    │                     │───────────────────>│                  │
     │                 │                    │                     │                   │                   │
     │                 │                    │                     │ 7. Encoded password│                  │
     │                 │                    │                     │<───────────────────│                  │
     │                 │                    │                     │                   │                   │
     │                 │                    │                     │ 8. Create Student  │                  │
     │                 │                    │                     │───────────────────>│                  │
     │                 │                    │                     │                   │                   │
     │                 │                    │                     │ 9. Student saved   │                  │
     │                 │                    │                     │<───────────────────│                  │
     │                 │                    │                     │                   │                   │
     │                 │                    │                     │ 10. UserCreationResult│              │
     │                 │                    │                     │───────────────────│                  │
     │                 │                    │                     │                   │                   │
     │                 │                    │ 11. sendAccountCredentials              │                   │
     │                 │                    │─────────────────────────────────────────>│                  │
     │                 │                    │                     │                   │                   │
     │                 │                    │                     │                   │ 12. Send email    │
     │                 │                    │                     │                   │──────────────────>│
     │                 │                    │                     │                   │                   │
     │                 │                    │                     │                   │ 13. Email sent   │
     │                 │                    │                     │                   │<──────────────────│
     │                 │                    │                     │                   │                   │
     │                 │                    │ 14. Success         │                   │                   │
     │                 │                    │<────────────────────│                  │                   │
     │                 │                    │                     │                   │                   │
     │                 │ 15. Redirect with  │                    │                     │                   │
     │                 │     success message│                    │                     │                   │
     │                 │<───────────────────│                    │                     │                   │
     │                 │                    │                     │                   │                   │
     │ 16. Success     │                    │                     │                   │                   │
     │     message     │                    │                     │                   │                   │
     │<────────────────│                    │                     │                   │                   │
     │                 │                    │                     │                   │                   │
```

## 6. Diagramme de Séquence - Attribution de Note (Formateur)

```
┌──────────┐    ┌──────────────┐    ┌──────────────────┐    ┌──────────────┐    ┌──────────────┐
│Formateur │    │ React App    │    │Grade             │    │Grade        │    │   Database   │
│          │    │              │    │RestController    │    │Service      │    │              │
└────┬─────┘    └──────┬───────┘    └────────┬─────────┘    └──────┬───────┘    └──────┬───────┘
     │                 │                    │                     │                   │
     │ 1. Select course│                    │                     │                   │
     │────────────────>│                    │                     │                   │
     │                 │                    │                     │                   │
     │                 │ 2. Load students   │                    │                     │                   │
     │                 │    for course      │                    │                     │                   │
     │                 │───────────────────│                    │                     │                   │
     │                 │                    │                     │                   │
     │                 │                    │ 3. GET /api/inscriptions/course/{id}    │
     │                 │                    │─────────────────────>│                  │
     │                 │                    │                     │                   │
     │                 │                    │                     │ 4. Find enrollments│
     │                 │                    │                     │───────────────────>│
     │                 │                    │                     │                   │
     │                 │                    │                     │ 5. Enrollments    │
     │                 │                    │                     │<───────────────────│
     │                 │                    │                     │                   │
     │                 │ 6. Students list   │                    │                     │                   │
     │                 │<───────────────────│                    │                     │                   │
     │                 │                    │                     │                   │
     │ 7. Fill grade   │                    │                     │                   │
     │    form         │                    │                     │                   │
     │────────────────>│                    │                     │                   │
     │                 │                    │                     │                   │
     │                 │ 8. POST /api/grades/create               │                   │
     │                 │    {studentId, coursId, valeur, commentaire}│                │                   │
     │                 │───────────────────>│                    │                     │                   │
     │                 │                    │                     │                   │
     │                 │                    │ 9. saveOrUpdate     │                   │
     │                 │                    │─────────────────────>│                 │
     │                 │                    │                     │                   │
     │                 │                    │                     │ 10. Save/Update   │
     │                 │                    │                     │───────────────────>│
     │                 │                    │                     │                   │
     │                 │                    │                     │ 11. Grade saved   │
     │                 │                    │                     │<───────────────────│
     │                 │                    │                     │                   │
     │                 │                    │ 12. GradeDto       │                   │
     │                 │                    │<────────────────────│                  │
     │                 │                    │                     │                   │
     │                 │ 13. Success       │                    │                     │                   │
     │                 │<───────────────────│                    │                     │                   │
     │                 │                    │                     │                   │
     │                 │ 14. Refresh grades │                    │                     │                   │
     │                 │    list           │                    │                     │                   │
     │                 │───────────────────│                    │                     │                   │
     │                 │                    │                     │                   │
     │ 15. Updated     │                    │                     │                   │
     │     grades list │                    │                     │                   │
     │<────────────────│                    │                     │                   │
     │                 │                    │                     │                   │
```

## 7. Diagramme d'Architecture Détaillé

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT (Navigateur)                               │
│  ┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐   │
│  │ Interface Admin    │  │ Interface Étudiant │  │ Interface Formateur │   │
│  │ (Thymeleaf SSR)    │  │ (React SPA)        │  │ (React SPA)         │   │
│  │                    │  │                    │  │                    │   │
│  │ - Dashboard        │  │ - Mes Cours         │  │ - Mes Cours         │   │
│  │ - Gestion CRUD     │  │ - Cours Disponibles│  │ - Gérer les Notes  │   │
│  │ - Création comptes │  │ - Mes Notes        │  │ - Fichiers         │   │
│  │                    │  │ - Emploi du Temps  │  │ - Planification    │   │
│  └─────────┬──────────┘  └──────────┬─────────┘  └──────────┬─────────┘   │
└────────────┼─────────────────────────┼─────────────────────────┼───────────┘
             │                         │                         │
             │ HTTP/HTTPS              │ HTTP/HTTPS              │ HTTP/HTTPS
             │                         │ (JWT Bearer Token)      │ (JWT Bearer Token)
             ▼                         ▼                         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        SERVEUR SPRING BOOT                                  │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐ │
│  │                    COUCHE PRÉSENTATION                                │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │ │
│  │  │ Controllers  │  │ REST         │  │ Security      │              │ │
│  │  │ Thymeleaf    │  │ Controllers  │  │ Filter        │              │ │
│  │  │              │  │              │  │ (JWT)         │              │ │
│  │  │ - Admin*     │  │ - Course*    │  │               │              │ │
│  │  │ - Login      │  │ - Grade*     │  │               │              │ │
│  │  │              │  │ - Enrollment*│  │               │              │ │
│  │  │              │  │ - Schedule*  │  │               │              │ │
│  │  │              │  │ - File*      │  │               │              │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘              │ │
│  └──────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│  ┌──────────────────────────────────────────────────────────────────────┐ │
│  │                    COUCHE SERVICE                                      │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │ │
│  │  │ Business     │  │ Email        │  │ Security     │              │ │
│  │  │ Services     │  │ Service      │  │ Service      │              │ │
│  │  │              │  │              │  │              │              │ │
│  │  │ - Student    │  │ - SMTP       │  │ - JWT        │              │ │
│  │  │ - Course     │  │ - Templates  │  │ - BCrypt     │              │ │
│  │  │ - Enrollment │  │              │  │              │              │ │
│  │  │ - Grade      │  │              │  │              │              │ │
│  │  │ - Schedule   │  │              │  │              │              │ │
│  │  │ - Registration│ │              │  │              │              │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘              │ │
│  └──────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│  ┌──────────────────────────────────────────────────────────────────────┐ │
│  │                    COUCHE PERSISTANCE                                 │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │ │
│  │  │ Repository   │  │ Entity       │  │ DTO          │              │ │
│  │  │ (JPA)        │  │ (JPA)        │  │ (Mapper)     │              │ │
│  │  │              │  │              │  │              │              │ │
│  │  │ - User*      │  │ - User       │  │ - CourseDto   │              │ │
│  │  │ - Student*   │  │ - Student    │  │ - GradeDto   │              │ │
│  │  │ - Course*    │  │ - Course     │  │ - EnrollmentDto│            │ │
│  │  │ - Grade*     │  │ - Grade      │  │              │              │ │
│  │  │ - Enrollment*│  │ - Enrollment │  │              │              │ │
│  │  │              │  │ - Schedule   │  │              │              │ │
│  │  │              │  │ - CourseFile │  │              │              │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘              │ │
│  └──────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ JDBC
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        BASE DE DONNÉES                                      │
│                           MySQL 8.0                                          │
│                                                                             │
│  Tables: users, students, trainers, courses, enrollments, grades,           │
│          schedules, course_files, sessions, groups, specialties, roles      │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 8. Diagramme de Déploiement

```
┌─────────────────────────────────────────────────────────────────┐
│                      ENVIRONNEMENT PRODUCTION                    │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                    SERVEUR WEB                            │ │
│  │                                                            │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  Application Spring Boot                              │ │ │
│  │  │  - Port: 8080                                         │ │ │
│  │  │  - Profil: prod                                       │ │ │
│  │  │  - JVM: Java 17                                       │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  │                                                            │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  Fichiers Statiques                                  │ │ │
│  │  │  - /react/etudiant/                                 │ │ │
│  │  │  - /react/formateur/                                 │ │ │
│  │  │  - /static/                                          │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  │                                                            │ │
│  └───────────────────────────────────────────────────────────┘ │
│                            │                                    │
│                            │ JDBC                               │
│                            ▼                                    │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                    SERVEUR BASE DE DONNÉES                 │ │
│  │                                                            │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  MySQL 8.0                                           │ │ │
│  │  │  - Port: 3306                                        │ │ │
│  │  │  - Base: formation_db                                │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  │                                                            │ │
│  └───────────────────────────────────────────────────────────┘ │
│                            │                                    │
│                            │ SMTP                               │
│                            ▼                                    │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                    SERVEUR EMAIL                          │ │
│  │                                                            │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  SMTP Server (Gmail, etc.)                           │ │ │
│  │  │  - Port: 587 (TLS)                                   │ │ │
│  │  │  - Authentification: OAuth2 / App Password          │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  │                                                            │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                            │
                            │ HTTPS
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CLIENTS                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │ Navigateur   │  │ Mobile App   │  │ API Clients  │        │
│  │ (Chrome,     │  │ (Future)     │  │ (Externes)   │        │
│  │  Firefox)    │  │              │  │              │        │
│  └──────────────┘  └──────────────┘  └──────────────┘        │
└─────────────────────────────────────────────────────────────────┘
```

## 9. Diagramme de Flux de Données - Calendrier Étudiant

```
┌─────────┐
│Étudiant │
└────┬────┘
     │
     │ 1. Accès à l'onglet "Emploi du Temps"
     ▼
┌─────────────────────┐
│  React Calendar     │
│  Component          │
└──────┬──────────────┘
       │
       │ 2. useEffect() - Chargement initial
       ▼
┌─────────────────────┐
│  apiService          │
│  getAllSchedules()   │
└──────┬──────────────┘
       │
       │ 3. GET /api/schedules/student/{id}/all
       ▼
┌─────────────────────┐
│  ScheduleRestController│
└──────┬──────────────┘
       │
       │ 4. getAllStudentSchedules()
       ▼
┌─────────────────────┐
│  ScheduleService     │
└──────┬──────────────┘
       │
       │ 5. Repository Query (JOIN FETCH)
       ▼
┌─────────────────────┐
│  ScheduleRepository  │
└──────┬──────────────┘
       │
       │ 6. SELECT avec JOIN
       ▼
┌─────────────────────┐
│  Database            │
│  (schedules table)   │
└──────┬──────────────┘
       │
       │ 7. List<Schedule>
       │    (avec Course, Student chargés)
       ▼
┌─────────────────────┐
│  ScheduleService     │
│  (retour)            │
└──────┬──────────────┘
       │
       │ 8. List<Schedule>
       ▼
┌─────────────────────┐
│  ScheduleRestController│
│  (JSON Response)     │
└──────┬──────────────┘
       │
       │ 9. JSON Array
       ▼
┌─────────────────────┐
│  React Calendar     │
│  (setSchedules)     │
└──────┬──────────────┘
       │
       │ 10. Rendu du calendrier
       │     - Calcul des jours du mois
       │     - Filtrage par date
       │     - Affichage dans les cellules
       ▼
┌─────────────────────┐
│  Calendrier Affiché │
│  avec Séances       │
└─────────────────────┘
```

## 10. Diagramme d'État - Séance (Schedule)

```
                    ┌─────────────┐
                    │   PENDING   │
                    │  (En attente)│
                    └──────┬──────┘
                           │
                           │ approve()
                           │
        ┌──────────────────┴──────────────────┐
        │                                      │
        ▼                                      ▼
┌─────────────┐                        ┌─────────────┐
│  APPROVED   │                        │  REJECTED    │
│ (Approuvé)  │                        │  (Rejeté)    │
└─────────────┘                        └─────────────┘
        │                                      │
        │                                      │
        │ update()                            │
        │                                      │
        ▼                                      ▼
┌──────────────────────────────────────────────────┐
│              MODIFICATIONS                       │
│  - Date, heure, salle                            │
│  - Cours associé                                │
└──────────────────────────────────────────────────┘
```

## 11. Diagramme de Composants React (Étudiant)

```
┌─────────────────────────────────────────────────────┐
│                  App Component                       │
│  - État: token, user, activeTab, refreshKey         │
│  - Gestion: login, logout, navigation               │
└──────────────┬──────────────────────────────────────┘
               │
       ┌───────┴────────┬──────────────┬──────────────┐
       │                │              │              │
       ▼                ▼              ▼              ▼
┌─────────────┐  ┌─────────────┐ ┌──────────┐  ┌──────────┐
│  Login      │  │ MyCourses   │ │Available │  │  Grades  │
│  Component  │  │ Component   │ │Courses   │  │ Component│
└─────────────┘  │             │ │Component │  │          │
                 │ - loadCourses│ │          │  │          │
                 │ - handleUnenroll│ │ - loadData│ │ - loadGrades│
                 └─────────────┘ │ - handleEnroll│ │ - loadAverage│
                                └──────────┘  └──────────┘
                                       │
                                       ▼
                                ┌──────────────┐
                                │  Calendar    │
                                │  Component   │
                                │              │
                                │ - loadSchedules│
                                │ - getDaysInMonth│
                                │ - getSchedulesForDay│
                                │ - Navigation │
                                │ - Filtrage   │
                                └──────────────┘
                                       │
                                       ▼
                                ┌──────────────┐
                                │ CourseCard   │
                                │ Component    │
                                │              │
                                │ - Affichage  │
                                │ - Actions   │
                                │ - Fichiers  │
                                └──────────────┘
```

## 12. Diagramme de Composants React (Formateur)

```
┌─────────────────────────────────────────────────────┐
│                  App Component                       │
│  - État: token, user, activeTab, refreshKey         │
│  - Gestion: login, logout, navigation               │
└──────────────┬──────────────────────────────────────┘
               │
       ┌───────┴────────┬──────────────┬──────────────┬──────────┐
       │               │              │              │          │
       ▼               ▼              ▼              ▼          ▼
┌─────────────┐  ┌─────────────┐ ┌──────────┐  ┌──────────┐ ┌──────────┐
│  Login      │  │  Courses   │ │GradeForm │  │CourseFiles│ │Schedule  │
│  Component  │  │  Component  │ │Component │  │Component │ │Management│
└─────────────┘  │             │ │          │  │          │ │Component │
                 │ - loadCourses│ │ - loadStudents│ │ - loadFiles│ │ - loadSchedules│
                 │ - createCourse│ │ - createGrade│ │ - upload │ │ - create │
                 │             │ │ - updateGrade│ │ - download│ │ - update │
                 └─────────────┘ └──────────┘  │ - delete │ │ - delete │
                                              └──────────┘ └──────────┘
```

## 13. Diagramme de Relations Base de Données (Détaillé)

```
┌──────────────┐
│    users      │
│──────────────│
│ id (PK)      │
│ username     │
│ password     │
│ email        │
│ nom          │
│ prenom       │
│ enabled      │
└──────┬───────┘
       │
       │ (1:1)
       │
   ┌───┴───┐
   │       │
   ▼       ▼
┌──────┐ ┌────────┐
│students│ │trainers│
│──────│ │────────│
│user_id│ │user_id │
│matricule│ │specialite│
│group_id│ └────────┘
│specialty_id│
└──────┬──────┘
       │
       │ (1:N)
       │
       ▼
┌──────────────┐      ┌──────────────┐
│ enrollments  │      │    grades    │
│──────────────│      │──────────────│
│ id (PK)      │      │ id (PK)      │
│ student_id ──┼──────┼─> student_id │
│ cours_id ────┼──────┼─> cours_id   │
│ date_inscrip │      │ valeur       │
└──────┬───────┘      │ commentaire │
       │              │ date_attrib │
       │              └──────┬───────┘
       │                     │
       │ (N:1)               │ (N:1)
       │                     │
       ▼                     ▼
┌──────────────┐      ┌──────────────┐
│   courses    │      │   courses    │
│──────────────│      │──────────────│
│ id (PK)      │      │ id (PK)      │
│ code         │      │ code         │
│ titre        │      │ titre        │
│ description  │      │ description  │
│ formateur_id │      │ formateur_id │
│ session_id ──┼──────┼─> session_id │
└──────┬───────┘      └──────┬───────┘
       │                      │
       │ (1:N)                │ (1:N)
       │                      │
       ▼                      ▼
┌──────────────┐      ┌──────────────┐
│  schedules   │      │  sessions    │
│──────────────│      │──────────────│
│ id (PK)      │      │ id (PK)      │
│ cours_id ────┼──────│ nom          │
│ date         │      │ type         │
│ heure_debut  │      │ date_debut   │
│ heure_fin    │      │ date_fin     │
│ salle        │      └──────────────┘
│ status       │
└──────────────┘

┌──────────────┐
│ course_files │
│──────────────│
│ id (PK)      │
│ cours_id ─────┼───┐
│ nom_fichier  │   │
│ type_mime    │   │ (N:1)
│ taille       │   │
│ chemin       │   │
│ date_upload │   │
└──────────────┘   │
                   │
                   ▼
            ┌──────────────┐
            │   courses    │
            └──────────────┘
```

## 14. Diagramme de Séquence - Upload de Fichier

```
┌──────────┐    ┌──────────────┐    ┌──────────────────┐    ┌──────────────┐    ┌──────────────┐
│Formateur │    │ React App   │    │CourseFile        │    │File Storage │    │   Database   │
│          │    │              │    │RestController    │    │(FileSystem) │    │              │
└────┬─────┘    └──────┬──────┘    └────────┬─────────┘    └──────┬──────┘    └──────┬───────┘
     │                 │                     │                     │                   │
     │ 1. Select file  │                     │                     │                   │
     │────────────────>│                     │                     │                   │
     │                 │                     │                     │                   │
     │                 │ 2. FormData         │                     │                   │
     │                 │    {file, courseId,  │                     │                   │
     │                 │     description}     │                     │                   │
     │                 │─────────────────────>│                     │                   │
     │                 │                     │                     │                   │
     │                 │                     │ 3. Save file        │                   │
     │                 │                     │─────────────────────>│                  │
     │                 │                     │                     │                   │
     │                 │                     │                     │ 4. File saved    │
     │                 │                     │                     │───────────────────>│
     │                 │                     │                     │                   │
     │                 │                     │                     │ 5. File path      │
     │                 │                     │                     │<───────────────────│
     │                 │                     │                     │                   │
     │                 │                     │ 6. Create CourseFile│                   │
     │                 │                     │─────────────────────────────────────────>│
     │                 │                     │                     │                   │
     │                 │                     │                     │ 7. CourseFile saved│
     │                 │                     │                     │<───────────────────│
     │                 │                     │                     │                   │
     │                 │                     │ 8. CourseFileDto   │                   │
     │                 │                     │─────────────────────│                  │
     │                 │                     │                     │                   │
     │                 │ 9. Success          │                     │                   │
     │                 │<────────────────────│                     │                   │
     │                 │                     │                     │                   │
     │                 │ 10. Refresh files   │                     │                   │
     │                 │     list           │                     │                   │
     │                 │─────────────────────│                     │                   │
     │                 │                     │                     │                   │
     │ 11. Updated     │                     │                     │                   │
     │     files list  │                     │                     │                   │
     │<────────────────│                     │                     │                   │
     │                 │                     │                     │                   │
```

## 15. Diagramme de Flux - Synchronisation UI React

```
┌─────────────────────────────────────────────────────────────┐
│                    Composant Parent (App)                   │
│  - refreshKey: state                                         │
│  - handleEnrollSuccess() → refreshKey++                     │
│  - handleUnenrollSuccess() → refreshKey++                   │
└──────────────┬───────────────────────────────────────────────┘
               │
       ┌───────┴────────┬──────────────┐
       │                │              │
       ▼                ▼              ▼
┌─────────────┐  ┌─────────────┐ ┌─────────────┐
│ MyCourses   │  │ Available   │ │  Calendar   │
│ Component   │  │ Courses     │ │ Component   │
│             │  │ Component   │ │             │
│ useEffect(  │  │ useEffect(  │ │ useEffect(  │
│  [refreshKey]│  │  [refreshKey]│ │  [refreshKey]│
│ )           │  │ )           │ │ )           │
│             │  │             │ │             │
│ loadCourses()│ │ loadData()  │ │ loadSchedules()│
└─────────────┘  └─────────────┘ └─────────────┘
       │                │              │
       │                │              │
       ▼                ▼              ▼
┌─────────────────────────────────────────────────────────────┐
│                    API REST Calls                            │
│  - GET /api/inscriptions/student/{id}                        │
│  - GET /api/cours                                            │
│  - GET /api/schedules/student/{id}/all                       │
└─────────────────────────────────────────────────────────────┘
       │                │              │
       │                │              │
       ▼                ▼              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Mise à Jour UI                            │
│  - Données rechargées                                        │
│  - Affichage synchronisé                                     │
│  - État cohérent                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Notes pour l'Intégration dans le Rapport

1. **Diagramme de Cas d'Utilisation** : À placer dans la section 8.1
2. **Diagramme de Classes** : À placer dans la section 8.2
3. **Diagrammes de Séquence** : À placer dans les sections 8.3, 8.4, etc.
4. **Diagramme d'Architecture** : À placer dans la section 8.5
5. **Diagrammes de Composants React** : Nouvelle section à ajouter
6. **Diagramme de Déploiement** : Section optionnelle mais recommandée

Ces diagrammes peuvent être convertis en images avec des outils comme:
- PlantUML
- Draw.io
- Lucidchart
- Mermaid (pour Markdown)

Ou intégrés directement dans Word en copiant le texte ASCII art.

