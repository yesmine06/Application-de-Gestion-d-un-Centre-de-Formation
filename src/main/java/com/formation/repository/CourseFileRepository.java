package com.formation.repository;

import com.formation.entity.CourseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseFileRepository extends JpaRepository<CourseFile, Long> {
    @Query("SELECT f FROM CourseFile f WHERE f.course.id = :courseId")
    List<CourseFile> findByCourseId(Long courseId);
    
    @Query("SELECT f FROM CourseFile f WHERE f.course.id IN :courseIds")
    List<CourseFile> findByCourseIdIn(List<Long> courseIds);
}

