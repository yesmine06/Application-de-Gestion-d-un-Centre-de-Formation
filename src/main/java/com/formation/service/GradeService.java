package com.formation.service;

import com.formation.entity.Grade;
import com.formation.entity.Student;
import com.formation.entity.Course;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.GradeRepository;
import com.formation.repository.StudentRepository;
import com.formation.repository.CourseRepository;
import com.formation.service.util.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GradeService extends EntityService<Grade, Long> {
    
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    
    @Autowired
    public GradeService(GradeRepository gradeRepository,
                       StudentRepository studentRepository,
                       CourseRepository courseRepository) {
        super(gradeRepository);
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }
    
    private GradeRepository getGradeRepository() {
        return (GradeRepository) repository;
    }
    
    public Grade saveOrUpdate(Long studentId, Long coursId, Double valeur, String commentaire) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + studentId));
        Course course = courseRepository.findById(coursId)
            .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé avec l'ID: " + coursId));
        
        Grade grade = getGradeRepository().findByStudentIdAndCoursId(studentId, coursId)
            .orElse(new Grade());
        
        grade.setStudent(student);
        grade.setCours(course);
        grade.setValeur(valeur);
        grade.setCommentaire(commentaire);
        
        return repository.save(grade);
    }
    
    public List<Grade> findByStudent(Long studentId) {
        return getGradeRepository().findByStudentId(studentId);
    }
    
    public List<Grade> findByCourse(Long coursId) {
        return getGradeRepository().findByCoursId(coursId);
    }
    
    public Double calculateStudentAverage(Long studentId) {
        return getGradeRepository().calculateAverageByStudentId(studentId);
    }
    
    public Double calculateCourseSuccessRate(Long coursId) {
        Long passing = getGradeRepository().countPassingGradesByCoursId(coursId);
        Long total = getGradeRepository().countTotalGradesByCoursId(coursId);
        
        if (total == 0) {
            return 0.0;
        }
        
        return (passing.doubleValue() / total.doubleValue()) * 100;
    }
    
    /**
     * Calcule la moyenne d'une liste de notes
     * Méthode utilitaire pour éviter la duplication de code
     */
    public static Double calculateAverage(List<Grade> grades) {
        if (grades == null || grades.isEmpty()) {
            return 0.0;
        }
        return grades.stream()
            .mapToDouble(Grade::getValeur)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Calcule la moyenne des notes d'un cours
     */
    public Double calculateCourseAverage(Long coursId) {
        List<Grade> grades = getGradeRepository().findByCoursId(coursId);
        return calculateAverage(grades);
    }
    
    /**
     * Supprime une note par ID
     */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}


