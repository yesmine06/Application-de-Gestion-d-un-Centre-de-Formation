package com.formation.service.util;

import com.formation.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service de base pour les opérations CRUD communes
 * Principe SOLID : DRY (Don't Repeat Yourself) et Open/Closed
 * 
 * @param <T> Type de l'entité
 * @param <ID> Type de l'ID
 */
public abstract class EntityService<T, ID> {
    
    protected final JpaRepository<T, ID> repository;
    
    protected EntityService(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }
    
    /**
     * Trouve une entité par ID ou lance une exception
     */
    protected T findByIdOrThrow(ID id, String entityName) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format("%s non trouvé(e) avec l'ID: %s", entityName, id)
            ));
    }
    
    /**
     * Trouve une entité par ID
     */
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }
    
    /**
     * Trouve toutes les entités
     */
    public List<T> findAll() {
        return repository.findAll();
    }
    
    /**
     * Sauvegarde une entité
     */
    public T save(T entity) {
        return repository.save(entity);
    }
    
    /**
     * Supprime une entité par ID
     */
    public void deleteById(ID id) {
        repository.deleteById(id);
    }
    
    /**
     * Vérifie si une entité existe
     */
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }
}

