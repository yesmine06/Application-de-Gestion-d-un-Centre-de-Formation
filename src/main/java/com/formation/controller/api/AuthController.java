package com.formation.controller.api;

import com.formation.config.JwtTokenProvider;
import com.formation.dto.JwtAuthenticationRequest;
import com.formation.dto.JwtAuthenticationResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    
    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody JwtAuthenticationRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
            
            String jwt = tokenProvider.generateToken(authentication);
            
            List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(new JwtAuthenticationResponse(
                jwt,
                authentication.getName(),
                roles
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                .body("{\"error\": \"Nom d'utilisateur ou mot de passe incorrect\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body("{\"error\": \"Erreur lors de l'authentification: " + e.getMessage() + "\"}");
        }
    }
}

