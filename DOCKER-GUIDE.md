# Guide Docker - Gestion Formation

## 📦 Modes d'utilisation

### 🚀 Production (Image figée)
Pour la production, utilisez l'image Docker optimisée qui contient une version figée de votre code.

**Après chaque modification du code :**
```bash
# Reconstruire l'image avec les changements
docker compose build

# OU reconstruire et redémarrer en une commande
docker compose up --build
```

### 🔧 Développement (Hot Reload)
Pour le développement avec rechargement automatique du code, utilisez la configuration de développement.

```bash
# Lancer avec hot reload activé
docker compose -f docker-compose.dev.yml up --build
```

## ⚠️ Important : Les changements ne sont PAS automatiques !

### Pourquoi ?
L'image Docker est construite une fois et contient une **copie figée** de votre code compilé dans un JAR. Les modifications que vous faites dans votre code local **ne sont pas automatiquement** dans l'image.

### Solutions

#### 1. **Mode Production (Reconstruction manuelle)**
Après chaque changement de code :
```bash
# Option 1 : Reconstruire et redémarrer
docker compose up --build

# Option 2 : Reconstruire séparément
docker compose build
docker compose up

# Option 3 : Reconstruire seulement le service app
docker compose build app
docker compose up -d app
```

#### 2. **Mode Développement (Hot Reload)**
Utilisez `docker-compose.dev.yml` qui :
- Monte votre code source local comme volume
- Utilise Spring Boot DevTools pour le rechargement automatique
- Permet de voir les changements sans reconstruire l'image

```bash
docker compose -f docker-compose.dev.yml up --build
```

⚠️ **Note** : Le hot reload fonctionne mieux pour les changements Java. Pour les changements majeurs (ajout de dépendances, etc.), vous devrez peut-être reconstruire.

#### 3. **Développement local (Sans Docker pour le code)**
Pour le développement actif, il est souvent plus rapide de :
```bash
# Lancer seulement MySQL dans Docker
docker compose up mysql -d

# Lancer l'application localement avec Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📋 Commandes utiles

### Voir les logs
```bash
# Tous les services
docker compose logs -f

# Seulement l'application
docker compose logs -f app

# Seulement MySQL
docker compose logs -f mysql
```

### Arrêter les services
```bash
# Arrêter sans supprimer
docker compose down

# Arrêter et supprimer les volumes (⚠️ supprime les données MySQL)
docker compose down -v
```

### Reconstruire une image spécifique
```bash
docker compose build app
```

### Accéder au conteneur
```bash
# Accéder au shell du conteneur app
docker exec -it formation-app bash

# Accéder au shell du conteneur MySQL
docker exec -it formation-mysql bash
```

## 🔄 Workflow recommandé

### Pour le développement quotidien
```bash
# 1. Lancer MySQL dans Docker
docker compose up mysql -d

# 2. Développer localement avec Maven (plus rapide, hot reload natif)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 3. Tester les changements immédiatement
```

### Pour tester l'image Docker
```bash
# Après avoir terminé une fonctionnalité
docker compose up --build

# Tester que tout fonctionne dans l'image
```

### Pour le déploiement
```bash
# 1. Reconstruire l'image avec les derniers changements
docker compose build

# 2. Taguer l'image (optionnel)
docker tag formation-app:latest votre-registry/formation-app:v1.0.0

# 3. Pousser vers un registry (optionnel)
docker push votre-registry/formation-app:v1.0.0
```

## 📝 Résumé

| Action | Commandes |
|--------|-----------|
| **Changement de code** | `docker compose up --build` |
| **Développement actif** | `docker compose up mysql -d` + `mvn spring-boot:run` |
| **Hot reload Docker** | `docker compose -f docker-compose.dev.yml up` |
| **Voir les logs** | `docker compose logs -f app` |
| **Arrêter** | `docker compose down` |

## ⚡ Performance

- **Première construction** : ~5-10 minutes (télécharge toutes les dépendances)
- **Reconstruction après changement de code** : ~2-5 minutes (utilise le cache Maven)
- **Hot reload (mode dev)** : Changements visibles en quelques secondes

