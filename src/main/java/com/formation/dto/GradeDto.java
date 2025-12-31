package com.formation.dto;

import com.formation.entity.Grade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO pour les notes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeDto implements Serializable {
    
    private Long id;
    private Long studentId;
    private String studentName;
    private Long coursId;
    private String coursTitre;
    private Double valeur;
    private String commentaire;
    
    public static GradeDto fromEntity(Grade grade) {
        GradeDto dto = new GradeDto();
        dto.setId(grade.getId());
        dto.setStudentId(grade.getStudent() != null ? grade.getStudent().getId() : null);
        dto.setStudentName(grade.getStudent() != null ? 
            grade.getStudent().getPrenom() + " " + grade.getStudent().getNom() : null);
        dto.setCoursId(grade.getCours() != null ? grade.getCours().getId() : null);
        dto.setCoursTitre(grade.getCours() != null ? grade.getCours().getTitre() : null);
        dto.setValeur(grade.getValeur());
        dto.setCommentaire(grade.getCommentaire());
        return dto;
    }
}

