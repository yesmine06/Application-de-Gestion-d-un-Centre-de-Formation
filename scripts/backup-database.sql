-- =====================================================
-- Script de Sauvegarde (Backup) de la Base de Données
-- Système de Gestion de Formation
-- =====================================================
-- 
-- Ce script permet de créer une sauvegarde complète
-- de la base de données MySQL.
--
-- Utilisation:
--   mysqldump -u [username] -p [database_name] > backup_YYYY-MM-DD.sql
--
-- Exemple:
--   mysqldump -u root -p formation_db > backup_2024-01-15.sql
--
-- Pour restaurer:
--   mysql -u [username] -p [database_name] < backup_YYYY-MM-DD.sql
--
-- =====================================================

-- =====================================================
-- OPTION 1: Sauvegarde complète avec mysqldump (RECOMMANDÉ)
-- =====================================================
-- 
-- Commande à exécuter dans le terminal:
-- 
-- Windows (PowerShell):
--   mysqldump -u root -p formation_db > backup_$(Get-Date -Format "yyyy-MM-dd_HH-mm-ss").sql
--
-- Linux/Mac:
--   mysqldump -u root -p formation_db > backup_$(date +%Y-%m-%d_%H-%M-%S).sql
--
-- Avec compression (recommandé pour grandes bases):
--   mysqldump -u root -p formation_db | gzip > backup_$(date +%Y-%m-%d).sql.gz
--
-- =====================================================

-- =====================================================
-- OPTION 2: Script SQL pour sauvegarde manuelle
-- =====================================================
-- 
-- Ce script peut être exécuté pour créer une sauvegarde
-- de la structure et des données importantes
--

-- Sauvegarde de la structure des tables
SHOW CREATE TABLE user;
SHOW CREATE TABLE student;
SHOW CREATE TABLE trainer;
SHOW CREATE TABLE role;
SHOW CREATE TABLE user_roles;
SHOW CREATE TABLE course;
SHOW CREATE TABLE enrollment;
SHOW CREATE TABLE grade;
SHOW CREATE TABLE session;
SHOW CREATE TABLE specialty;
SHOW CREATE TABLE `group`;
SHOW CREATE TABLE course_groups;
SHOW CREATE TABLE schedule;
SHOW CREATE TABLE course_file;

-- =====================================================
-- OPTION 3: Export des données importantes
-- =====================================================

-- Export des utilisateurs (sans mots de passe en clair)
SELECT id, username, email, nom, prenom, created_at 
FROM user 
ORDER BY created_at DESC;

-- Export des étudiants
SELECT 
    s.id,
    s.matricule,
    s.nom,
    s.prenom,
    s.email,
    s.date_inscription,
    sp.nom as specialite,
    g.nom as groupe
FROM student s
LEFT JOIN specialty sp ON s.specialty_id = sp.id
LEFT JOIN `group` g ON s.group_id = g.id
ORDER BY s.date_inscription DESC;

-- Export des formateurs
SELECT 
    t.id,
    t.nom,
    t.prenom,
    t.email,
    t.specialite
FROM trainer t
ORDER BY t.nom;

-- Export des cours
SELECT 
    c.id,
    c.code,
    c.titre,
    c.description,
    t.nom as formateur_nom,
    t.prenom as formateur_prenom,
    s.nom as session_nom,
    s.type as session_type
FROM course c
LEFT JOIN trainer t ON c.formateur_id = t.id
LEFT JOIN session s ON c.session_id = s.id
ORDER BY c.code;

-- Export des inscriptions
SELECT 
    e.id,
    e.date_inscription,
    s.matricule as etudiant_matricule,
    s.nom as etudiant_nom,
    s.prenom as etudiant_prenom,
    c.code as cours_code,
    c.titre as cours_titre
FROM enrollment e
JOIN student s ON e.student_id = s.id
JOIN course c ON e.course_id = c.id
ORDER BY e.date_inscription DESC;

-- Export des notes
SELECT 
    g.id,
    g.valeur,
    g.commentaire,
    g.date_attribution,
    s.matricule as etudiant_matricule,
    s.nom as etudiant_nom,
    s.prenom as etudiant_prenom,
    c.code as cours_code,
    c.titre as cours_titre
FROM grade g
JOIN student s ON g.student_id = s.id
JOIN course c ON g.course_id = c.id
ORDER BY g.date_attribution DESC;

-- Export du planning
SELECT 
    sch.id,
    sch.date,
    sch.heure_debut,
    sch.heure_fin,
    sch.salle,
    sch.status,
    c.code as cours_code,
    c.titre as cours_titre,
    t.nom as formateur_nom,
    t.prenom as formateur_prenom
FROM schedule sch
JOIN course c ON sch.course_id = c.id
JOIN trainer t ON c.formateur_id = t.id
WHERE sch.status = 'APPROVED'
ORDER BY sch.date, sch.heure_debut;

-- =====================================================
-- OPTION 4: Script de sauvegarde automatique (Bash)
-- =====================================================
-- 
-- Créer un fichier backup.sh avec le contenu suivant:
--
-- #!/bin/bash
-- DB_USER="root"
-- DB_NAME="formation_db"
-- BACKUP_DIR="/path/to/backups"
-- DATE=$(date +%Y-%m-%d_%H-%M-%S)
-- BACKUP_FILE="$BACKUP_DIR/backup_$DATE.sql"
-- 
-- # Créer le dossier de backup s'il n'existe pas
-- mkdir -p $BACKUP_DIR
-- 
-- # Effectuer la sauvegarde
-- mysqldump -u $DB_USER -p$DB_PASSWORD $DB_NAME > $BACKUP_FILE
-- 
-- # Compresser la sauvegarde
-- gzip $BACKUP_FILE
-- 
-- # Supprimer les sauvegardes de plus de 30 jours
-- find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +30 -delete
-- 
-- echo "Sauvegarde terminée: $BACKUP_FILE.gz"
--
-- =====================================================

-- =====================================================
-- OPTION 5: Script de sauvegarde automatique (PowerShell)
-- =====================================================
-- 
-- Créer un fichier backup.ps1 avec le contenu suivant:
--
-- $DB_USER = "root"
-- $DB_NAME = "formation_db"
-- $DB_PASSWORD = "your_password"
-- $BACKUP_DIR = "D:\backups"
-- $DATE = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
-- $BACKUP_FILE = "$BACKUP_DIR\backup_$DATE.sql"
-- 
-- # Créer le dossier de backup s'il n'existe pas
-- if (-not (Test-Path $BACKUP_DIR)) {
--     New-Item -ItemType Directory -Path $BACKUP_DIR
-- }
-- 
-- # Effectuer la sauvegarde
-- $env:MYSQL_PWD = $DB_PASSWORD
-- mysqldump -u $DB_USER $DB_NAME > $BACKUP_FILE
-- 
-- # Compresser la sauvegarde (nécessite 7-Zip ou WinRAR)
-- # Compress-Archive -Path $BACKUP_FILE -DestinationPath "$BACKUP_FILE.zip"
-- 
-- # Supprimer les sauvegardes de plus de 30 jours
-- Get-ChildItem -Path $BACKUP_DIR -Filter "backup_*.sql" | 
--     Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-30) } | 
--     Remove-Item
-- 
-- Write-Host "Sauvegarde terminée: $BACKUP_FILE"
--
-- =====================================================

-- =====================================================
-- OPTION 6: Sauvegarde avec Docker
-- =====================================================
-- 
-- Si vous utilisez Docker Compose:
--
-- # Sauvegarder la base de données
-- docker exec formation-mysql mysqldump -u root -p[password] formation_db > backup.sql
--
-- # Restaurer la base de données
-- docker exec -i formation-mysql mysql -u root -p[password] formation_db < backup.sql
--
-- # Avec compression
-- docker exec formation-mysql mysqldump -u root -p[password] formation_db | gzip > backup.sql.gz
--
-- =====================================================

-- =====================================================
-- NOTES IMPORTANTES
-- =====================================================
-- 
-- 1. Mots de passe: Les mots de passe sont hashés (BCrypt)
--    et ne peuvent pas être restaurés en clair.
--    Après restauration, utilisez la fonction "Mot de passe oublié"
--    ou réinitialisez les mots de passe via l'interface admin.
--
-- 2. Fichiers uploadés: Ce script ne sauvegarde PAS les fichiers
--    uploadés dans le système de fichiers. Sauvegardez séparément
--    le dossier uploads/ si vous en avez un.
--
-- 3. Fréquence recommandée: Effectuez une sauvegarde quotidienne
--    en production, hebdomadaire en développement.
--
-- 4. Test de restauration: Testez régulièrement la restauration
--    de vos sauvegardes pour vous assurer qu'elles fonctionnent.
--
-- 5. Stockage: Conservez les sauvegardes dans un endroit sûr,
--    de préférence sur un serveur distant ou un service cloud.
--
-- =====================================================

