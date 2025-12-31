package com.formation.service;

import com.formation.dto.CourseDto;
import com.formation.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseDtoMapper {
    
    public CourseDto toDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setCode(course.getCode());
        dto.setTitre(course.getTitre());
        dto.setDescription(course.getDescription());
        
        if (course.getFormateur() != null) {
            dto.setFormateurId(course.getFormateur().getId());
            dto.setFormateurNom(course.getFormateur().getNom());
            dto.setFormateurPrenom(course.getFormateur().getPrenom());
        }
        
        if (course.getSession() != null) {
            dto.setSessionId(course.getSession().getId());
            dto.setSessionNom(course.getSession().getNom());
        }
        
        return dto;
    }
}

