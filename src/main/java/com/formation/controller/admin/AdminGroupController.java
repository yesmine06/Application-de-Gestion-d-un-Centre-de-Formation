package com.formation.controller.admin;

import com.formation.entity.Group;
import com.formation.service.GroupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/groups")
public class AdminGroupController extends BaseAdminController<Group, Long> {
    
    private final GroupService groupService;
    
    public AdminGroupController(GroupService groupService) {
        super("group", "groups", "groups");
        this.groupService = groupService;
    }
    
    @GetMapping
    public String listGroups(Model model) {
        return list(model, groupService.findAll());
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        return showCreateForm(model, Group::new);
    }
    
    @PostMapping
    public String createGroup(@ModelAttribute Group group, RedirectAttributes redirectAttributes) {
        return handleCreate(() -> groupService.save(group), redirectAttributes);
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return showEditForm(id, model, groupService.findById(id));
    }
    
    @PostMapping("/{id}")
    public String updateGroup(@PathVariable Long id, @ModelAttribute Group group, 
                             RedirectAttributes redirectAttributes) {
        return handleUpdate(() -> groupService.update(id, group), redirectAttributes);
    }
    
    @GetMapping("/{id}/delete")
    public String deleteGroup(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return handleDelete(() -> groupService.delete(id), redirectAttributes);
    }
}

