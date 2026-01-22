# Guide d'Utilisation des Diagrammes PlantUML

## Fichier : `DIAGRAMMES-PLANTUML.puml`

Ce fichier contient tous les diagrammes UML du projet au format PlantUML.

## Comment Utiliser

### 1. En Ligne (Recommandé)

1. Allez sur [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
2. Copiez le code d'un diagramme (entre `@startuml` et `@enduml`)
3. Collez dans l'éditeur
4. Cliquez sur "Submit"
5. Téléchargez l'image (PNG, SVG, ou PDF)

### 2. Avec Visual Studio Code

1. Installez l'extension "PlantUML"
2. Ouvrez le fichier `.puml`
3. Appuyez sur `Alt+D` pour prévisualiser
4. Exportez en PNG/SVG avec `Ctrl+Shift+P` → "PlantUML: Export Current Diagram"

### 3. Avec IntelliJ IDEA

1. Installez le plugin "PlantUML integration"
2. Ouvrez le fichier `.puml`
3. Clic droit → "PlantUML" → "Generate Diagram"
4. Exportez l'image

### 4. En Ligne de Commande

```bash
# Installer PlantUML (nécessite Java)
# Windows: choco install plantuml
# Linux: sudo apt-get install plantuml
# Mac: brew install plantuml

# Générer toutes les images
plantuml DIAGRAMMES-PLANTUML.puml

# Générer une image spécifique (extraire le diagramme d'abord)
plantuml -o output/ diagramme.puml
```

## Liste des Diagrammes

### 1. Diagramme_Cas_Utilisation
- **Description** : Cas d'utilisation pour Admin, Formateur, Étudiant
- **Usage** : Section 8.1 du rapport

### 2. Diagramme_Classes
- **Description** : Modèle de classes avec relations
- **Usage** : Section 8.2 du rapport

### 3. Sequence_Authentification_JWT
- **Description** : Flux d'authentification avec JWT
- **Usage** : Section 8.4 du rapport

### 4. Sequence_Inscription_Cours
- **Description** : Processus d'inscription à un cours
- **Usage** : Section 8.3 du rapport

### 5. Sequence_Creation_Compte_Admin
- **Description** : Création de compte par admin avec envoi email
- **Usage** : Section 8.6 du rapport

### 6. Sequence_Attribution_Note
- **Description** : Attribution de note par un formateur
- **Usage** : Section 8.3 du rapport

### 7. Sequence_Upload_Fichier
- **Description** : Upload de fichier par un formateur
- **Usage** : Section 8.3 du rapport

### 8. Architecture_Couches
- **Description** : Architecture en couches du système
- **Usage** : Section 2.1 du rapport

### 9. Architecture_Deploiement
- **Description** : Architecture de déploiement
- **Usage** : Section 8.5 du rapport

### 10. Composants_React_Etudiant
- **Description** : Structure des composants React pour étudiants
- **Usage** : Section 9.1 du rapport

### 11. Composants_React_Formateur
- **Description** : Structure des composants React pour formateurs
- **Usage** : Section 9.1 du rapport

### 12. Etat_Schedule
- **Description** : Machine à états pour les séances
- **Usage** : Section 9.2 du rapport

### 13. Relations_Base_Donnees
- **Description** : Schéma entité-relation de la base de données
- **Usage** : Section 4.1 du rapport

### 14. Flux_Calendrier_Etudiant
- **Description** : Flux de chargement du calendrier
- **Usage** : Section 8.3 du rapport

### 15. Synchronisation_UI_React
- **Description** : Mécanisme de synchronisation UI
- **Usage** : Section 5.4 du rapport

### 16. Flux_Attribution_Note
- **Description** : Flux complet d'attribution de note
- **Usage** : Section 8.3 du rapport

### 17. Architecture_Complete
- **Description** : Architecture complète du système
- **Usage** : Section 2.1 du rapport

## Personnalisation

### Changer le thème

Remplacez `!theme plain` par :
- `!theme cerulean`
- `!theme reddress-darkred`
- `!theme sketchy-outline`
- `!theme carbon-gray`

### Changer les couleurs

Ajoutez après `@startuml` :
```plantuml
skinparam class {
    BackgroundColor LightBlue
    BorderColor DarkBlue
}
```

### Exporter en différents formats

- **PNG** : Par défaut, bonne qualité
- **SVG** : Vectoriel, scalable
- **PDF** : Pour documents imprimés
- **EPS** : Pour LaTeX

## Intégration dans Word

1. Générez les images en PNG (haute résolution)
2. Dans Word : Insertion → Images → Insérer l'image
3. Ajustez la taille si nécessaire
4. Ajoutez une légende : Clic droit → Insérer une légende

## Intégration dans LaTeX

```latex
\begin{figure}[h]
    \centering
    \includegraphics[width=0.8\textwidth]{diagramme.png}
    \caption{Description du diagramme}
    \label{fig:diagramme}
\end{figure}
```

## Conseils

1. **Résolution** : Utilisez au moins 300 DPI pour l'impression
2. **Format** : PNG pour Word, SVG pour web, PDF pour impression
3. **Taille** : Ajustez la taille dans PlantUML avec `scale 1.5` si nécessaire
4. **Légendes** : Ajoutez toujours des légendes et numéros de figure

## Exemple de Commande pour Générer Tous les Diagrammes

```bash
# Générer tous les diagrammes en PNG haute résolution
plantuml -tpng -SDPI=300 DIAGRAMMES-PLANTUML.puml

# Générer en SVG (vectoriel)
plantuml -tsvg DIAGRAMMES-PLANTUML.puml

# Générer en PDF
plantuml -tpdf DIAGRAMMES-PLANTUML.puml
```

## Dépannage

### Erreur "Java not found"
- Installez Java JDK 8 ou supérieur
- Vérifiez que `java` est dans le PATH

### Diagrammes trop petits
- Ajoutez `scale 1.5` ou `scale 2` au début du diagramme
- Utilisez `-SDPI=300` en ligne de commande

### Caractères spéciaux mal affichés
- Utilisez UTF-8 pour l'encodage du fichier
- Évitez les caractères spéciaux dans les noms

