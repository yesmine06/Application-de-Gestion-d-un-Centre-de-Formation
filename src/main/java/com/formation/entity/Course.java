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

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Le code du cours est requis")
    private String code;
    
    @Column(nullable = false)
    @NotBlank(message = "Le titre du cours est requis")
    private String titre;
    
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "formateur_id", nullable = false)
    @JsonIgnoreProperties({"courses", "roles"})
    @NotNull(message = "Le formateur est requis")
    private Trainer formateur;
    
    @ManyToOne
    @JoinColumn(name = "session_id")
    @JsonIgnoreProperties({"courses"}) // Éviter la récursion infinie avec Session
    private Session session;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_groups",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Group> groups = new ArrayList<>();
    
    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"cours"})
    private List<Enrollment> enrollments = new ArrayList<>();
    
    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"cours", "student"}) // Éviter la récursion infinie avec Grade
    private List<Grade> grades = new ArrayList<>();
    
    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"cours"})
    private List<Schedule> schedules = new ArrayList<>();
}


