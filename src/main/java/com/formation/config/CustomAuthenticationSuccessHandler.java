package com.formation.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
        String targetUrl = determineTargetUrl(request, authentication);
        
        logger.debug("Authentication successful for user: {}, redirecting to: {}", 
                     authentication.getName(), targetUrl);
        
        if (response.isCommitted()) {
            logger.warn("Response already committed, cannot redirect to: {}", targetUrl);
            return;
        }
        
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    protected String determineTargetUrl(HttpServletRequest request, Authentication authentication) {
        // Récupérer le rôle choisi dans le formulaire
        String selectedRole = request.getParameter("role");
        
        // Récupérer les rôles réels de l'utilisateur
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // Si un rôle est sélectionné, vérifier que l'utilisateur l'a et rediriger
        if (selectedRole != null && !selectedRole.isEmpty()) {
            String roleToCheck = "ROLE_" + selectedRole.toUpperCase();
            
            // Vérifier que l'utilisateur a bien ce rôle
            boolean hasRole = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleToCheck));
            
            if (hasRole) {
                return getUrlForRole(selectedRole.toUpperCase());
            } else {
                // Si l'utilisateur n'a pas le rôle sélectionné, rediriger vers login avec erreur
                return "/login?error=role";
            }
        }
        
        // Sinon, rediriger selon le premier rôle de l'utilisateur
        if (!authorities.isEmpty()) {
            String firstRole = authorities.iterator().next().getAuthority().replace("ROLE_", "");
            return getUrlForRole(firstRole);
        }
        
        // Par défaut, rediriger vers le dashboard
        return "/dashboard";
    }
    
    private String getUrlForRole(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN" -> "/admin/dashboard";
            case "FORMATEUR" -> "/formateur/dashboard";
            case "ETUDIANT" -> "/etudiant/dashboard";
            default -> "/dashboard";
        };
    }
}

