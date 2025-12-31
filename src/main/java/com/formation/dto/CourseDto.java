package com.formation.dto;

import lombok.Data;

@Data
public class CourseDto {
    private Long id;
    private String code;
    private String titre;
    private String description;
    private Long formateurId;
    private String formateurNom;
    private String formateurPrenom;
    private Long sessionId;
    private String sessionNom;
}

