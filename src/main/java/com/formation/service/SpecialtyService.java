package com.formation.service;

import com.formation.entity.Specialty;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.SpecialtyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SpecialtyService {
    
    @Autowired
    private SpecialtyRepository specialtyRepository;
    
    @Cacheable(value = "specialties", key = "'all'")
    public List<Specialty> findAll() {
        return specialtyRepository.findAll();
    }
    
    @Cacheable(value = "specialties", key = "#id")
    public Optional<Specialty> findById(Long id) {
        return specialtyRepository.findById(id);
    }
    
    @CacheEvict(value = "specialties", allEntries = true)
    public Specialty save(Specialty specialty) {
        return specialtyRepository.save(specialty);
    }
    
    @CacheEvict(value = "specialties", allEntries = true)
    public Specialty update(Long id, Specialty specialtyDetails) {
        Specialty specialty = specialtyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Spécialité non trouvée"));
        
        specialty.setNom(specialtyDetails.getNom());
        specialty.setDescription(specialtyDetails.getDescription());
        
        return specialtyRepository.save(specialty);
    }
    
    @CacheEvict(value = "specialties", allEntries = true)
    public void delete(Long id) {
        specialtyRepository.deleteById(id);
    }
}


