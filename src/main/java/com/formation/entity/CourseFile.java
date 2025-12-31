package com.formation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String originalFileName;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private Long fileSize;
    
    private String contentType;
    
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime uploadDate = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}

