package com.formation.controller.api;

import com.formation.dto.StudentDto;
import com.formation.entity.Student;
import com.formation.exception.ResourceNotFoundException;
import com.formation.service.StudentDtoMapper;
import com.formation.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
public class StudentRestController extends BaseRestController<Student, Long, StudentDto> {
    
    private final StudentService studentService;
    private final StudentDtoMapper studentDtoMapper;
    
    public StudentRestController(StudentService studentService, StudentDtoMapper studentDtoMapper) {
        this.studentService = studentService;
        this.studentDtoMapper = studentDtoMapper;
    }
    
    @Override
    protected Student findByIdOrThrow(Long id) {
        return studentService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + id));
    }
    
    @Override
    protected List<Student> findAll() {
        return studentService.findAll();
    }
    
    @Override
    protected Page<Student> findAll(Pageable pageable) {
        return studentService.findAll(pageable);
    }
    
    @Override
    protected Student save(Student entity) {
        return studentService.save(entity);
    }
    
    @Override
    protected Student update(Long id, Student entity) {
        return studentService.update(id, entity);
    }
    
    @Override
    protected void delete(Long id) {
        studentService.delete(id);
    }
    
    @Override
    protected StudentDto toDto(Student entity) {
        return studentDtoMapper.toDto(entity);
    }
    
    @Override
    protected String getEntityName() {
        return "Étudiant";
    }
}


