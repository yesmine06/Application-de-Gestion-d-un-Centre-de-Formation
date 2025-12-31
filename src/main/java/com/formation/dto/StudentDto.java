package com.formation.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StudentDto {
    private Long id;
    private String username;
    private String email;
    private String nom;
    private String prenom;
    private String matricule;
    private LocalDate dateInscription;
    private Long specialtyId;
    private String specialtyName;
    private Long groupId;
    private String groupName;
    private boolean enabled;
}

