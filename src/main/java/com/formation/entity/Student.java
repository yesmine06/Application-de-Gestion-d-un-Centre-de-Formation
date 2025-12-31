package com.formation.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "user_id")
public class Student extends User {
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Le matricule est requis")
    private String matricule;
    
    @Column(nullable = false)
    @NotNull(message = "La date d'inscription est requise")
    private LocalDate dateInscription;
    
    @ManyToOne
    @JoinColumn(name = "specialty_id")
    @JsonIgnoreProperties({"students"}) // Ignorer la liste des étudiants pour éviter la référence circulaire
    private Specialty specialty;
    
    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonIgnoreProperties({"students", "courses"}) // Ignorer la liste des étudiants et cours pour éviter la référence circulaire
    private Group group;
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"student"})
    private List<Enrollment> enrollments = new ArrayList<>();
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"student"})
    private List<Grade> grades = new ArrayList<>();
}


