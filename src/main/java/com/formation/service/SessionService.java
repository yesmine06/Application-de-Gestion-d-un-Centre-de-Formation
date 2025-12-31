package com.formation.service;

import com.formation.entity.Session;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SessionService {
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Cacheable(value = "sessions", key = "'all'")
    public List<Session> findAll() {
        return sessionRepository.findAll();
    }
    
    @Cacheable(value = "sessions", key = "#id")
    public Optional<Session> findById(Long id) {
        return sessionRepository.findById(id);
    }
    
    @CacheEvict(value = "sessions", allEntries = true)
    public Session save(Session session) {
        return sessionRepository.save(session);
    }
    
    @CacheEvict(value = "sessions", allEntries = true)
    public Session update(Long id, Session sessionDetails) {
        Session session = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée"));
        
        session.setNom(sessionDetails.getNom());
        session.setDateDebut(sessionDetails.getDateDebut());
        session.setDateFin(sessionDetails.getDateFin());
        session.setType(sessionDetails.getType());
        
        return sessionRepository.save(session);
    }
    
    @CacheEvict(value = "sessions", allEntries = true)
    public void delete(Long id) {
        sessionRepository.deleteById(id);
    }
}


