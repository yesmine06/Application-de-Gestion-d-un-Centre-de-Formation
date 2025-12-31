package com.formation.service;

import com.formation.entity.Group;
import com.formation.exception.ResourceNotFoundException;
import com.formation.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GroupService {
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Cacheable(value = "groups", key = "'all'")
    public List<Group> findAll() {
        return groupRepository.findAll();
    }
    
    @Cacheable(value = "groups", key = "#id")
    public Optional<Group> findById(Long id) {
        return groupRepository.findById(id);
    }
    
    @CacheEvict(value = "groups", allEntries = true)
    public Group save(Group group) {
        return groupRepository.save(group);
    }
    
    @CacheEvict(value = "groups", allEntries = true)
    public Group update(Long id, Group groupDetails) {
        Group group = groupRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Groupe non trouvé"));
        
        group.setNom(groupDetails.getNom());
        group.setDescription(groupDetails.getDescription());
        
        return groupRepository.save(group);
    }
    
    @CacheEvict(value = "groups", allEntries = true)
    public void delete(Long id) {
        groupRepository.deleteById(id);
    }
}


