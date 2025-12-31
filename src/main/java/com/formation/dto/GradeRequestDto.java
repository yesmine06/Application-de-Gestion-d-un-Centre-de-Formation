package com.formation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeRequestDto {
    
    @NotNull(message = "L'ID de l'étudiant est requis")
    private Long studentId;
    
    @NotNull(message = "L'ID du cours est requis")
    private Long coursId;
    
    @NotNull(message = "La valeur de la note est requise")
    @DecimalMin(value = "0.0", message = "La note doit être supérieure ou égale à 0")
    @DecimalMax(value = "20.0", message = "La note doit être inférieure ou égale à 20")
    private Double valeur;
    
    private String commentaire;
}

