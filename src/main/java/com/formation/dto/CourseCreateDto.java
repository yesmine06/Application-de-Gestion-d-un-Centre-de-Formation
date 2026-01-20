package com.formation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO pour la création de cours par un formateur
 * Le formateur est automatiquement assigné depuis l'authentification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateDto implements Serializable {
    
    @NotBlank(message = "Le code du cours est requis")
    private String code;
    
    @NotBlank(message = "Le titre du cours est requis")
    private String titre;
    
    private String description;
    
    private Long sessionId;
}

