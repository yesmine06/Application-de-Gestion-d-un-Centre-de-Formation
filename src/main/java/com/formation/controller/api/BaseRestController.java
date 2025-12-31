package com.formation.controller.api;

import com.formation.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur de base pour les opérations CRUD REST communes
 * Principe SOLID : DRY (Don't Repeat Yourself) et Open/Closed
 * 
 * @param <T> Type de l'entité
 * @param <ID> Type de l'ID
 * @param <DTO> Type du DTO
 */
public abstract class BaseRestController<T, ID, DTO> {
    
    protected abstract T findByIdOrThrow(ID id);
    protected abstract List<T> findAll();
    protected abstract Page<T> findAll(Pageable pageable);
    protected abstract T save(T entity);
    protected abstract T update(ID id, T entity);
    protected abstract void delete(ID id);
    protected abstract DTO toDto(T entity);
    protected abstract String getEntityName();
    
    /**
     * GET / - Liste paginée ou complète
     */
    @GetMapping
    public ResponseEntity<?> getAll(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Boolean paginated) {
        
        if (Boolean.TRUE.equals(paginated)) {
            Page<T> page = findAll(pageable);
            Page<DTO> dtoPage = page.map(this::toDto);
            return ResponseEntity.ok(dtoPage);
        }
        
        List<T> entities = findAll();
        List<DTO> dtos = entities.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /{id} - Trouver par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DTO> getById(@PathVariable ID id) {
        T entity = findByIdOrThrow(id);
        return ResponseEntity.ok(toDto(entity));
    }
    
    /**
     * POST / - Créer
     */
    @PostMapping
    public ResponseEntity<T> create(@Valid @RequestBody T entity) {
        T saved = save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    /**
     * PUT /{id} - Mettre à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<T> updateEntity(@PathVariable ID id, @Valid @RequestBody T entity) {
        T updated = update(id, entity);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * DELETE /{id} - Supprimer
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntity(@PathVariable ID id) {
        delete(id);
        return ResponseEntity.noContent().build();
    }
}

