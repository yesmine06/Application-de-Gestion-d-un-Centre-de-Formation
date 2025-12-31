package com.formation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentRequestDto {
    
    @NotNull(message = "L'ID de l'étudiant est requis")
    private Long studentId;
    
    @NotNull(message = "L'ID du cours est requis")
    private Long coursId;
}

