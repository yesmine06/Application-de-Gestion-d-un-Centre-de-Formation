package com.formation.repository;

import com.formation.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByMatricule(String matricule);
    boolean existsByMatricule(String matricule);
    boolean existsByUsername(String username);
    
    @Query("SELECT s FROM Student s WHERE s.specialty.id = :specialtyId")
    List<Student> findBySpecialtyId(Long specialtyId);
    
    @Query("SELECT s FROM Student s WHERE s.group.id = :groupId")
    List<Student> findByGroupId(Long groupId);
}


