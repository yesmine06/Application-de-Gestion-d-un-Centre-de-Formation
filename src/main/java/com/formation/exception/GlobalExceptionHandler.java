package com.formation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.formation.exception.BusinessException;
import com.formation.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Gère les erreurs de validation (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Erreur de validation",
            errors.toString(),
            request.getDescription(false)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Gère les erreurs de contrainte d'intégrité (doublons, clés étrangères, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        String message = "Erreur de contrainte de données";
        String errorMessage = ex.getMessage();
        
        // Messages d'erreur plus conviviaux
        if (errorMessage != null) {
            if (errorMessage.contains("username") || errorMessage.contains("UK_USERNAME")) {
                message = "Ce nom d'utilisateur est déjà utilisé";
            } else if (errorMessage.contains("email") || errorMessage.contains("UK_EMAIL")) {
                message = "Cet email est déjà utilisé";
            } else if (errorMessage.contains("matricule") || errorMessage.contains("UK_MATRICULE")) {
                message = "Ce matricule est déjà utilisé";
            } else if (errorMessage.contains("foreign key") || errorMessage.contains("FK_")) {
                message = "Impossible de supprimer cet élément car il est référencé ailleurs";
            }
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            message,
            errorMessage,
            request.getDescription(false)
        );
        
        logger.warn("DataIntegrityViolation: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Gère les erreurs d'authentification
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        
        // Ne pas exposer le message d'erreur exact pour éviter l'énumération d'utilisateurs
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.UNAUTHORIZED.value(),
            "Nom d'utilisateur ou mot de passe incorrect",
            "Les identifiants fournis sont incorrects",
            request.getDescription(false)
        );
        
        logger.warn("Tentative d'authentification échouée pour: {}", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Gère les erreurs d'accès refusé
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.FORBIDDEN.value(),
            "Accès refusé",
            "Vous n'avez pas les permissions nécessaires pour accéder à cette ressource",
            request.getDescription(false)
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * Gère les erreurs de ressource non trouvée
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Ressource non trouvée",
            ex.getMessage(),
            request.getDescription(false)
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Gère les erreurs de validation métier
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Erreur de validation",
            ex.getMessage(),
            request.getDescription(false)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Gère les erreurs métier générales
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Erreur métier",
            ex.getMessage(),
            request.getDescription(false)
        );
        
        logger.warn("BusinessException: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Gère les erreurs de ressource statique non trouvée (favicon, etc.)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex, WebRequest request) {
        
        // Ignorer les erreurs pour favicon.ico et autres ressources statiques
        String path = request.getDescription(false).replace("uri=", "");
        if (path.contains("/favicon.ico") || path.contains("/logout")) {
            // Pour logout, retourner une redirection vers login
            if (path.contains("/logout")) {
                return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/login?logout")
                    .build();
            }
            // Pour favicon, retourner 404 silencieux
            return ResponseEntity.notFound().build();
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Ressource non trouvée",
            ex.getMessage(),
            request.getDescription(false)
        );
        
        logger.debug("Ressource non trouvée: {}", path);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Gère toutes les autres exceptions non gérées
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        // Ne pas exposer le message d'erreur complet en production
        String errorMessage = ex.getMessage();
        String safeMessage = "Une erreur interne s'est produite";
        
        // En développement, on peut montrer plus de détails
        boolean isDevelopment = System.getProperty("spring.profiles.active", "").contains("dev");
        
        if (isDevelopment && errorMessage != null) {
            safeMessage = errorMessage;
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Une erreur interne s'est produite",
            safeMessage,
            request.getDescription(false)
        );
        
        // Logger l'erreur complète côté serveur, mais ne pas l'exposer au client
        logger.error("Erreur non gérée: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Classe interne pour représenter les réponses d'erreur
     */
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        
        public ErrorResponse(LocalDateTime timestamp, int status, String error, 
                           String message, String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }
        
        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
    }
}

