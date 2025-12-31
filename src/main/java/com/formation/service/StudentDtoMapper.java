package com.formation.service;

import com.formation.dto.StudentDto;
import com.formation.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentDtoMapper {
    
    public StudentDto toDto(Student student) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setUsername(student.getUsername());
        dto.setEmail(student.getEmail());
        dto.setNom(student.getNom());
        dto.setPrenom(student.getPrenom());
        dto.setMatricule(student.getMatricule());
        dto.setDateInscription(student.getDateInscription());
        dto.setEnabled(student.isEnabled());
        
        if (student.getSpecialty() != null) {
            dto.setSpecialtyId(student.getSpecialty().getId());
            dto.setSpecialtyName(student.getSpecialty().getNom());
        }
        
        if (student.getGroup() != null) {
            dto.setGroupId(student.getGroup().getId());
            dto.setGroupName(student.getGroup().getNom());
        }
        
        return dto;
    }
}

