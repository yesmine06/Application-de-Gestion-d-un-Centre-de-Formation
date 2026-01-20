# =====================================================
# Script de Sauvegarde Automatique (PowerShell)
# Système de Gestion de Formation
# =====================================================
#
# Ce script effectue une sauvegarde automatique de la
# base de données MySQL.
#
# Utilisation:
#   .\backup.ps1
#
# Configuration:
#   Modifiez les variables ci-dessous selon votre environnement
# =====================================================

# Configuration
$DB_USER = "root"
$DB_NAME = "formation_db"
$DB_PASSWORD = ""  # Laissez vide pour être invité à saisir
$DB_HOST = "localhost"
$DB_PORT = "3307"  # Port par défaut pour dev (3306 pour prod)
$BACKUP_DIR = ".\backups"
$RETENTION_DAYS = 30  # Nombre de jours de rétention des sauvegardes

# Obtenir la date et l'heure pour le nom du fichier
$DATE = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$BACKUP_FILE = "$BACKUP_DIR\backup_$DATE.sql"
$BACKUP_FILE_ZIP = "$BACKUP_FILE.zip"

# Créer le dossier de backup s'il n'existe pas
if (-not (Test-Path $BACKUP_DIR)) {
    New-Item -ItemType Directory -Path $BACKUP_DIR -Force | Out-Null
    Write-Host "Dossier de backup créé: $BACKUP_DIR" -ForegroundColor Green
}

# Demander le mot de passe si non fourni
if ([string]::IsNullOrEmpty($DB_PASSWORD)) {
    $securePassword = Read-Host "Entrez le mot de passe MySQL pour '$DB_USER'" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $DB_PASSWORD = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
}

# Vérifier si mysqldump est disponible
$mysqldumpPath = Get-Command mysqldump -ErrorAction SilentlyContinue
if (-not $mysqldumpPath) {
    Write-Host "ERREUR: mysqldump n'est pas trouvé dans le PATH." -ForegroundColor Red
    Write-Host "Assurez-vous que MySQL est installé et que mysqldump est dans votre PATH." -ForegroundColor Yellow
    exit 1
}

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Sauvegarde de la base de données" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Base de données: $DB_NAME" -ForegroundColor White
Write-Host "Utilisateur: $DB_USER" -ForegroundColor White
Write-Host "Hôte: $DB_HOST:$DB_PORT" -ForegroundColor White
Write-Host "Fichier de sauvegarde: $BACKUP_FILE" -ForegroundColor White
Write-Host ""

# Effectuer la sauvegarde
Write-Host "Sauvegarde en cours..." -ForegroundColor Yellow
try {
    # Définir le mot de passe comme variable d'environnement (plus sûr que -p)
    $env:MYSQL_PWD = $DB_PASSWORD
    
    # Exécuter mysqldump
    & mysqldump -h $DB_HOST -P $DB_PORT -u $DB_USER --single-transaction --routines --triggers $DB_NAME > $BACKUP_FILE
    
    # Vérifier si la sauvegarde a réussi
    if ($LASTEXITCODE -eq 0 -and (Test-Path $BACKUP_FILE) -and (Get-Item $BACKUP_FILE).Length -gt 0) {
        $fileSize = (Get-Item $BACKUP_FILE).Length / 1MB
        Write-Host "✓ Sauvegarde réussie!" -ForegroundColor Green
        Write-Host "  Taille: $([math]::Round($fileSize, 2)) MB" -ForegroundColor White
        
        # Compresser la sauvegarde (optionnel)
        Write-Host ""
        Write-Host "Compression de la sauvegarde..." -ForegroundColor Yellow
        try {
            Compress-Archive -Path $BACKUP_FILE -DestinationPath $BACKUP_FILE_ZIP -Force
            Remove-Item $BACKUP_FILE
            Write-Host "✓ Sauvegarde compressée: $BACKUP_FILE_ZIP" -ForegroundColor Green
            $zipSize = (Get-Item $BACKUP_FILE_ZIP).Length / 1MB
            Write-Host "  Taille compressée: $([math]::Round($zipSize, 2)) MB" -ForegroundColor White
        } catch {
            Write-Host "⚠ Compression échouée, sauvegarde non compressée conservée" -ForegroundColor Yellow
        }
        
        # Nettoyer les anciennes sauvegardes
        Write-Host ""
        Write-Host "Nettoyage des anciennes sauvegardes (plus de $RETENTION_DAYS jours)..." -ForegroundColor Yellow
        $deletedCount = 0
        Get-ChildItem -Path $BACKUP_DIR -Filter "backup_*.sql*" | 
            Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-$RETENTION_DAYS) } | 
            ForEach-Object { 
                Remove-Item $_.FullName -Force
                $deletedCount++
            }
        if ($deletedCount -gt 0) {
            Write-Host "✓ $deletedCount ancienne(s) sauvegarde(s) supprimée(s)" -ForegroundColor Green
        } else {
            Write-Host "✓ Aucune ancienne sauvegarde à supprimer" -ForegroundColor Green
        }
        
        Write-Host ""
        Write-Host "================================================" -ForegroundColor Cyan
        Write-Host "Sauvegarde terminée avec succès!" -ForegroundColor Green
        Write-Host "================================================" -ForegroundColor Cyan
        
    } else {
        Write-Host "✗ ERREUR: La sauvegarde a échoué" -ForegroundColor Red
        if (Test-Path $BACKUP_FILE) {
            Remove-Item $BACKUP_FILE -Force
        }
        exit 1
    }
} catch {
    Write-Host "✗ ERREUR lors de la sauvegarde: $_" -ForegroundColor Red
    exit 1
} finally {
    # Nettoyer la variable d'environnement
    Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
}

