# 📦 Guide de Sauvegarde de la Base de Données

Ce dossier contient les scripts et la documentation pour sauvegarder et restaurer la base de données du système de gestion de formation.

## 📋 Fichiers Disponibles

- **`backup-database.sql`** : Documentation complète et requêtes SQL pour la sauvegarde
- **`backup.ps1`** : Script PowerShell pour Windows (sauvegarde automatique)
- **`backup.sh`** : Script Bash pour Linux/Mac (sauvegarde automatique)

---

## 🚀 Utilisation Rapide

### Option 1 : Sauvegarde Manuelle (Recommandé)

#### Windows (PowerShell)
```powershell
mysqldump -u root -p formation_db > backup_2024-01-15.sql
```

#### Linux/Mac
```bash
mysqldump -u root -p formation_db > backup_2024-01-15.sql
```

#### Avec Docker
```bash
docker exec formation-mysql mysqldump -u root -p[password] formation_db > backup.sql
```

---

### Option 2 : Script Automatique PowerShell

1. **Modifier la configuration** dans `backup.ps1` :
   ```powershell
   $DB_USER = "root"
   $DB_NAME = "formation_db"
   $DB_PASSWORD = ""  # Laissez vide pour être invité
   $DB_PORT = "3307"  # 3307 pour dev, 3306 pour prod
   ```

2. **Exécuter le script** :
   ```powershell
   .\scripts\backup.ps1
   ```

Le script va :
- ✅ Créer une sauvegarde complète
- ✅ Compresser la sauvegarde (optionnel)
- ✅ Supprimer les sauvegardes de plus de 30 jours
- ✅ Afficher un rapport détaillé

---

### Option 3 : Script Automatique Bash

1. **Rendre le script exécutable** :
   ```bash
   chmod +x scripts/backup.sh
   ```

2. **Modifier la configuration** dans `backup.sh` :
   ```bash
   DB_USER="root"
   DB_NAME="formation_db"
   DB_PORT="3307"  # 3307 pour dev, 3306 pour prod
   ```

3. **Exécuter le script** :
   ```bash
   ./scripts/backup.sh
   ```

---

## 🔄 Restauration

### Restauration Manuelle

#### Windows (PowerShell)
```powershell
mysql -u root -p formation_db < backup_2024-01-15.sql
```

#### Linux/Mac
```bash
mysql -u root -p formation_db < backup_2024-01-15.sql
```

#### Avec Docker
```bash
docker exec -i formation-mysql mysql -u root -p[password] formation_db < backup.sql
```

---

## 📅 Planification Automatique (Cron/Task Scheduler)

### Windows - Planificateur de Tâches

1. Ouvrir le **Planificateur de tâches**
2. Créer une **tâche de base**
3. Déclencher : **Quotidien** à 2h00
4. Action : **Démarrer un programme**
   - Programme : `powershell.exe`
   - Arguments : `-File "D:\chemin\vers\scripts\backup.ps1"`

### Linux/Mac - Cron

Ajouter à `crontab -e` :
```bash
# Sauvegarde quotidienne à 2h00
0 2 * * * /chemin/vers/scripts/backup.sh >> /var/log/backup.log 2>&1
```

---

## ⚠️ Notes Importantes

1. **Mots de passe** : Les mots de passe sont hashés (BCrypt) et ne peuvent pas être restaurés en clair. Après restauration, utilisez la fonction "Mot de passe oublié" ou réinitialisez via l'interface admin.

2. **Fichiers uploadés** : Les scripts ne sauvegardent PAS les fichiers uploadés dans le système de fichiers. Sauvegardez séparément le dossier `uploads/` si vous en avez un.

3. **Fréquence recommandée** :
   - **Production** : Sauvegarde quotidienne
   - **Développement** : Sauvegarde hebdomadaire

4. **Test de restauration** : Testez régulièrement la restauration de vos sauvegardes pour vous assurer qu'elles fonctionnent.

5. **Stockage** : Conservez les sauvegardes dans un endroit sûr, de préférence sur un serveur distant ou un service cloud.

---

## 🔍 Vérification d'une Sauvegarde

Pour vérifier qu'une sauvegarde est valide :

```bash
# Vérifier la taille du fichier (doit être > 0)
ls -lh backup_*.sql

# Vérifier le contenu (doit contenir CREATE TABLE)
head -n 50 backup_*.sql

# Tester la restauration sur une base de test
mysql -u root -p test_db < backup_*.sql
```

---

## 📊 Statistiques de Sauvegarde

Les scripts affichent automatiquement :
- ✅ Taille de la sauvegarde
- ✅ Taille compressée (si compression activée)
- ✅ Nombre de sauvegardes supprimées
- ✅ Statut de l'opération

---

## 🆘 Dépannage

### Erreur : "mysqldump n'est pas trouvé"
- **Windows** : Ajoutez MySQL au PATH ou utilisez le chemin complet : `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe`
- **Linux/Mac** : Installez MySQL : `sudo apt-get install mysql-client` (Ubuntu) ou `brew install mysql-client` (Mac)

### Erreur : "Access denied"
- Vérifiez le nom d'utilisateur et le mot de passe
- Vérifiez les permissions MySQL de l'utilisateur

### Erreur : "Database not found"
- Vérifiez que la base de données existe : `SHOW DATABASES;`
- Vérifiez le nom de la base dans la configuration

---

## 📝 Exemple de Sortie

```
================================================
Sauvegarde de la base de données
================================================
Base de données: formation_db
Utilisateur: root
Hôte: localhost:3307
Fichier de sauvegarde: .\backups\backup_2024-01-15_14-30-00.sql

Sauvegarde en cours...
✓ Sauvegarde réussie!
  Taille: 2.45 MB

Compression de la sauvegarde...
✓ Sauvegarde compressée: .\backups\backup_2024-01-15_14-30-00.sql.zip
  Taille compressée: 0.78 MB

Nettoyage des anciennes sauvegardes (plus de 30 jours)...
✓ 3 ancienne(s) sauvegarde(s) supprimée(s)

================================================
Sauvegarde terminée avec succès!
================================================
```

---

Pour plus de détails, consultez `backup-database.sql` qui contient la documentation complète.

