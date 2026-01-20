#!/bin/bash
# =====================================================
# Script de Sauvegarde Automatique (Bash)
# Système de Gestion de Formation
# =====================================================
#
# Ce script effectue une sauvegarde automatique de la
# base de données MySQL.
#
# Utilisation:
#   chmod +x backup.sh
#   ./backup.sh
#
# Configuration:
#   Modifiez les variables ci-dessous selon votre environnement
# =====================================================

# Configuration
DB_USER="root"
DB_NAME="formation_db"
DB_PASSWORD=""  # Laissez vide pour être invité à saisir
DB_HOST="localhost"
DB_PORT="3307"  # Port par défaut pour dev (3306 pour prod)
BACKUP_DIR="./backups"
RETENTION_DAYS=30  # Nombre de jours de rétention des sauvegardes

# Obtenir la date et l'heure pour le nom du fichier
DATE=$(date +%Y-%m-%d_%H-%M-%S)
BACKUP_FILE="$BACKUP_DIR/backup_$DATE.sql"
BACKUP_FILE_GZ="$BACKUP_FILE.gz"

# Créer le dossier de backup s'il n'existe pas
mkdir -p "$BACKUP_DIR"

# Demander le mot de passe si non fourni
if [ -z "$DB_PASSWORD" ]; then
    read -sp "Entrez le mot de passe MySQL pour '$DB_USER': " DB_PASSWORD
    echo ""
fi

# Vérifier si mysqldump est disponible
if ! command -v mysqldump &> /dev/null; then
    echo "ERREUR: mysqldump n'est pas trouvé."
    echo "Assurez-vous que MySQL est installé et que mysqldump est dans votre PATH."
    exit 1
fi

echo "================================================"
echo "Sauvegarde de la base de données"
echo "================================================"
echo "Base de données: $DB_NAME"
echo "Utilisateur: $DB_USER"
echo "Hôte: $DB_HOST:$DB_PORT"
echo "Fichier de sauvegarde: $BACKUP_FILE"
echo ""

# Effectuer la sauvegarde
echo "Sauvegarde en cours..."
if mysqldump -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" \
    --single-transaction --routines --triggers "$DB_NAME" > "$BACKUP_FILE" 2>/dev/null; then
    
    # Vérifier si la sauvegarde a réussi
    if [ -f "$BACKUP_FILE" ] && [ -s "$BACKUP_FILE" ]; then
        FILE_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
        echo "✓ Sauvegarde réussie!"
        echo "  Taille: $FILE_SIZE"
        
        # Compresser la sauvegarde
        echo ""
        echo "Compression de la sauvegarde..."
        if gzip -c "$BACKUP_FILE" > "$BACKUP_FILE_GZ"; then
            rm "$BACKUP_FILE"
            GZ_SIZE=$(du -h "$BACKUP_FILE_GZ" | cut -f1)
            echo "✓ Sauvegarde compressée: $BACKUP_FILE_GZ"
            echo "  Taille compressée: $GZ_SIZE"
        else
            echo "⚠ Compression échouée, sauvegarde non compressée conservée"
        fi
        
        # Nettoyer les anciennes sauvegardes
        echo ""
        echo "Nettoyage des anciennes sauvegardes (plus de $RETENTION_DAYS jours)..."
        DELETED_COUNT=$(find "$BACKUP_DIR" -name "backup_*.sql*" -type f -mtime +$RETENTION_DAYS -delete -print | wc -l)
        if [ "$DELETED_COUNT" -gt 0 ]; then
            echo "✓ $DELETED_COUNT ancienne(s) sauvegarde(s) supprimée(s)"
        else
            echo "✓ Aucune ancienne sauvegarde à supprimer"
        fi
        
        echo ""
        echo "================================================"
        echo "Sauvegarde terminée avec succès!"
        echo "================================================"
        
    else
        echo "✗ ERREUR: La sauvegarde a échoué (fichier vide ou inexistant)"
        [ -f "$BACKUP_FILE" ] && rm "$BACKUP_FILE"
        exit 1
    fi
else
    echo "✗ ERREUR: La sauvegarde a échoué"
    [ -f "$BACKUP_FILE" ] && rm "$BACKUP_FILE"
    exit 1
fi

