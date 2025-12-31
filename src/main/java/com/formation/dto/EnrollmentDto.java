package com.formation.dto;

import com.formation.entity.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO pour les inscriptions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDto implements Serializable {
    
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentMatricule;
    private Long coursId;
    private String coursTitre;
    private String coursCode;
    
    public static EnrollmentDto fromEntity(Enrollment enrollment) {
        EnrollmentDto dto = new EnrollmentDto();
        dto.setId(enrollment.getId());
        dto.setStudentId(enrollment.getStudent() != null ? enrollment.getStudent().getId() : null);
        dto.setStudentName(enrollment.getStudent() != null ? 
            enrollment.getStudent().getPrenom() + " " + enrollment.getStudent().getNom() : null);
        dto.setStudentMatricule(enrollment.getStudent() != null ? 
            enrollment.getStudent().getMatricule() : null);
        dto.setCoursId(enrollment.getCours() != null ? enrollment.getCours().getId() : null);
        dto.setCoursTitre(enrollment.getCours() != null ? enrollment.getCours().getTitre() : null);
        dto.setCoursCode(enrollment.getCours() != null ? enrollment.getCours().getCode() : null);
        return dto;
    }
}

