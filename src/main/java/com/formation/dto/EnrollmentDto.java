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
    // Ajout d'un objet cours pour faciliter l'utilisation côté frontend
    private CourseDto cours;
    // Ajout d'un objet student pour faciliter l'utilisation côté frontend
    private StudentDto student;
    
    public static EnrollmentDto fromEntity(Enrollment enrollment) {
        EnrollmentDto dto = new EnrollmentDto();
        dto.setId(enrollment.getId());
        
        if (enrollment.getStudent() != null) {
            dto.setStudentId(enrollment.getStudent().getId());
            dto.setStudentName(enrollment.getStudent().getPrenom() + " " + enrollment.getStudent().getNom());
            dto.setStudentMatricule(enrollment.getStudent().getMatricule());
            
            // Créer un objet StudentDto pour faciliter l'utilisation côté frontend
            StudentDto studentDto = new StudentDto();
            studentDto.setId(enrollment.getStudent().getId());
            studentDto.setNom(enrollment.getStudent().getNom());
            studentDto.setPrenom(enrollment.getStudent().getPrenom());
            studentDto.setMatricule(enrollment.getStudent().getMatricule());
            studentDto.setEmail(enrollment.getStudent().getEmail());
            studentDto.setUsername(enrollment.getStudent().getUsername());
            dto.setStudent(studentDto);
        }
        
        if (enrollment.getCours() != null) {
            dto.setCoursId(enrollment.getCours().getId());
            dto.setCoursTitre(enrollment.getCours().getTitre());
            dto.setCoursCode(enrollment.getCours().getCode());
            // Créer un objet CourseDto pour faciliter l'utilisation côté frontend
            CourseDto courseDto = new CourseDto();
            courseDto.setId(enrollment.getCours().getId());
            courseDto.setTitre(enrollment.getCours().getTitre());
            courseDto.setCode(enrollment.getCours().getCode());
            courseDto.setDescription(enrollment.getCours().getDescription());
            dto.setCours(courseDto);
        }
        
        return dto;
    }
}

