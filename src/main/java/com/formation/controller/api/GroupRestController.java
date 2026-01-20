package com.formation.controller.api;

import com.formation.entity.Group;
import com.formation.entity.Student;
import com.formation.exception.ResourceNotFoundException;
import com.formation.service.GroupService;
import com.formation.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN')")
public class GroupRestController {
    
    private final GroupService groupService;
    private final StudentService studentService;
    
    @Autowired
    public GroupRestController(GroupService groupService, StudentService studentService) {
        this.groupService = groupService;
        this.studentService = studentService;
    }
    
    /**
     * GET / - Liste de tous les groupes
     */
    @GetMapping
    public ResponseEntity<List<Group>> getAllGroups() {
        List<Group> groups = groupService.findAll();
        return ResponseEntity.ok(groups);
    }
    
    /**
     * GET /{id} - Obtenir un groupe par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        Group group = groupService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Groupe non trouvé"));
        return ResponseEntity.ok(group);
    }
    
    /**
     * GET /{id}/students - Obtenir les étudiants d'un groupe
     */
    @GetMapping("/{id}/students")
    public ResponseEntity<List<Student>> getGroupStudents(@PathVariable Long id) {
        Group group = groupService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Groupe non trouvé"));
        List<Student> students = studentService.findByGroup(id);
        return ResponseEntity.ok(students);
    }
    
    /**
     * POST / - Créer un nouveau groupe (admin uniquement)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Group> createGroup(@Valid @RequestBody Group group) {
        Group savedGroup = groupService.save(group);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGroup);
    }
    
    /**
     * PUT /{id} - Mettre à jour un groupe (admin uniquement)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Group> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody Group groupDetails) {
        Group updatedGroup = groupService.update(id, groupDetails);
        return ResponseEntity.ok(updatedGroup);
    }
    
    /**
     * DELETE /{id} - Supprimer un groupe (admin uniquement)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

