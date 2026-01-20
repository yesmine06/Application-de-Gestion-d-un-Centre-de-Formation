package com.formation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    /**
     * Vérifie si le service email est configuré
     */
    public boolean isEmailConfigured() {
        return mailSender != null;
    }
    
    /**
     * Envoie une notification d'inscription à un étudiant
     */
    public void sendEnrollmentNotification(String to, String studentName, String courseTitle) {
        try {
            if (mailSender == null) {
                // Mock service - log détaillé
                logger.info("=== MOCK EMAIL SERVICE (JavaMailSender non configuré) ===");
                logger.info("📧 Notification d'inscription");
                logger.info("   Destinataire: {}", to);
                logger.info("   Étudiant: {}", studentName);
                logger.info("   Cours: {}", courseTitle);
                logger.info("   Sujet: Inscription au cours: {}", courseTitle);
                logger.info("=========================================================");
                return;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isEmpty()) {
                message.setFrom(fromEmail); // Utiliser l'email configuré comme expéditeur
            }
            message.setTo(to);
            message.setSubject("Inscription au cours: " + courseTitle);
            message.setText("Bonjour " + studentName + ",\n\n" +
                           "Vous avez été inscrit(e) au cours: " + courseTitle + ".\n\n" +
                           "Cordialement,\nL'équipe de gestion");
            
            mailSender.send(message);
            logger.info("✅ Email d'inscription envoyé avec succès à: {} pour le cours: {}", to, courseTitle);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi de l'email d'inscription à {}: {}", to, e.getMessage(), e);
            // On ne propage pas l'exception pour ne pas bloquer l'inscription
        }
    }
    
    /**
     * Envoie une notification de désinscription à un étudiant
     */
    public void sendUnenrollmentNotification(String to, String studentName, String courseTitle) {
        try {
            if (mailSender == null) {
                // Mock service - log détaillé
                logger.info("=== MOCK EMAIL SERVICE (JavaMailSender non configuré) ===");
                logger.info("📧 Notification de désinscription");
                logger.info("   Destinataire: {}", to);
                logger.info("   Étudiant: {}", studentName);
                logger.info("   Cours: {}", courseTitle);
                logger.info("   Sujet: Désinscription du cours: {}", courseTitle);
                logger.info("=========================================================");
                return;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isEmpty()) {
                message.setFrom(fromEmail); // Utiliser l'email configuré comme expéditeur
            }
            message.setTo(to);
            message.setSubject("Désinscription du cours: " + courseTitle);
            message.setText("Bonjour " + studentName + ",\n\n" +
                           "Vous avez été désinscrit(e) du cours: " + courseTitle + ".\n\n" +
                           "Cordialement,\nL'équipe de gestion");
            
            mailSender.send(message);
            logger.info("✅ Email de désinscription envoyé avec succès à: {} pour le cours: {}", to, courseTitle);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi de l'email de désinscription à {}: {}", to, e.getMessage(), e);
            // On ne propage pas l'exception pour ne pas bloquer la désinscription
        }
    }
    
    /**
     * Notifie un formateur lorsqu'un étudiant s'inscrit ou se désinscrit à son cours
     */
    public void notifyTrainer(String to, String trainerName, String studentName, String courseTitle, boolean enrolled) {
        try {
            String action = enrolled ? "inscrit" : "désinscrit";
            
            if (mailSender == null) {
                // Mock service - log détaillé
                logger.info("=== MOCK EMAIL SERVICE (JavaMailSender non configuré) ===");
                logger.info("📧 Notification formateur");
                logger.info("   Destinataire: {}", to);
                logger.info("   Formateur: {}", trainerName);
                logger.info("   Étudiant: {}", studentName);
                logger.info("   Cours: {}", courseTitle);
                logger.info("   Action: L'étudiant s'est {}", action);
                logger.info("   Sujet: Notification - Cours: {}", courseTitle);
                logger.info("=========================================================");
                return;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isEmpty()) {
                message.setFrom(fromEmail); // Utiliser l'email configuré comme expéditeur
            }
            message.setTo(to);
            message.setSubject("Notification - Cours: " + courseTitle);
            message.setText("Bonjour " + trainerName + ",\n\n" +
                           "L'étudiant " + studentName + 
                           (enrolled ? " s'est inscrit" : " s'est désinscrit") +
                           " au cours: " + courseTitle + ".\n\n" +
                           "Cordialement,\nL'équipe de gestion");
            
            mailSender.send(message);
            logger.info("✅ Email de notification envoyé avec succès au formateur {} pour le cours: {}", to, courseTitle);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi de l'email de notification au formateur {}: {}", to, e.getMessage(), e);
            // On ne propage pas l'exception pour ne pas bloquer l'inscription/désinscription
        }
    }
    
    /**
     * Envoie une notification de séance planifiée à un étudiant
     */
    public void sendScheduleNotification(String to, String studentName, String courseTitle,
                                         java.time.LocalDate date, java.time.LocalTime heureDebut,
                                         java.time.LocalTime heureFin, String salle) {
        try {
            if (mailSender == null) {
                // Mock service - log détaillé
                logger.info("=== MOCK EMAIL SERVICE (JavaMailSender non configuré) ===");
                logger.info("📧 Notification de séance planifiée");
                logger.info("   Destinataire: {}", to);
                logger.info("   Étudiant: {}", studentName);
                logger.info("   Cours: {}", courseTitle);
                logger.info("   Date: {}", date);
                logger.info("   Heure: {} - {}", heureDebut, heureFin);
                logger.info("   Salle: {}", salle != null ? salle : "Non spécifiée");
                logger.info("   Sujet: Nouvelle séance planifiée: {}", courseTitle);
                logger.info("=========================================================");
                return;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isEmpty()) {
                message.setFrom(fromEmail);
            }
            message.setTo(to);
            message.setSubject("Nouvelle séance planifiée: " + courseTitle);
            
            String salleText = salle != null && !salle.isEmpty() ? salle : "Non spécifiée";
            message.setText("Bonjour " + studentName + ",\n\n" +
                           "Une nouvelle séance a été planifiée pour le cours: " + courseTitle + ".\n\n" +
                           "Détails de la séance:\n" +
                           "- Date: " + date + "\n" +
                           "- Heure: " + heureDebut + " - " + heureFin + "\n" +
                           "- Salle: " + salleText + "\n\n" +
                           "Cordialement,\nL'équipe de gestion");
            
            mailSender.send(message);
            logger.info("✅ Email de notification de séance envoyé avec succès à: {} pour le cours: {}", to, courseTitle);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi de l'email de notification de séance à {}: {}", to, e.getMessage(), e);
            // On ne propage pas l'exception pour ne pas bloquer la planification
        }
    }
    
    /**
     * Envoie les coordonnées de connexion à un nouvel utilisateur créé par l'admin
     */
    public void sendAccountCredentials(String to, String fullName, String username, String password, String userType) {
        try {
            String userTypeLabel = "ETUDIANT".equals(userType) ? "Étudiant" : "FORMATEUR".equals(userType) ? "Formateur" : "Utilisateur";
            
            if (mailSender == null) {
                // Mock service - log détaillé
                logger.info("=== MOCK EMAIL SERVICE (JavaMailSender non configuré) ===");
                logger.info("📧 Envoi des coordonnées de connexion");
                logger.info("   Destinataire: {}", to);
                logger.info("   Nom complet: {}", fullName);
                logger.info("   Type d'utilisateur: {}", userTypeLabel);
                logger.info("   Nom d'utilisateur: {}", username);
                logger.info("   Mot de passe: {}", password);
                logger.info("   Sujet: Vos coordonnées de connexion - Gestion Formation");
                logger.info("=========================================================");
                return;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isEmpty()) {
                message.setFrom(fromEmail);
            }
            message.setTo(to);
            message.setSubject("Vos coordonnées de connexion - Gestion Formation");
            
            String emailBody = String.format(
                "Bonjour %s,\n\n" +
                "Votre compte %s a été créé avec succès sur la plateforme de Gestion Formation.\n\n" +
                "Voici vos coordonnées de connexion :\n" +
                "- Nom d'utilisateur : %s\n" +
                "- Mot de passe : %s\n\n" +
                "⚠️ IMPORTANT : Pour des raisons de sécurité, veuillez changer votre mot de passe après votre première connexion.\n\n" +
                "Vous pouvez vous connecter à l'adresse suivante :\n" +
                "http://localhost:8080/login\n\n" +
                "Cordialement,\n" +
                "L'équipe de gestion",
                fullName, userTypeLabel, username, password
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            logger.info("✅ Email de coordonnées de connexion envoyé avec succès à: {} (username: {})", to, username);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi de l'email de coordonnées de connexion à {}: {}", to, e.getMessage(), e);
            // On ne propage pas l'exception pour ne pas bloquer la création du compte
        }
    }
}


