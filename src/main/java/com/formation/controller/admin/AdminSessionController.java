package com.formation.controller.admin;

import com.formation.entity.Session;
import com.formation.service.SessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sessions")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminSessionController extends BaseAdminController<Session, Long> {
    
    private final SessionService sessionService;
    
    public AdminSessionController(SessionService sessionService) {
        super("session", "sessions", "sessions");
        this.sessionService = sessionService;
    }
    
    @Override
    protected void prepareCreateForm(Model model) {
        model.addAttribute("sessionTypes", Session.SessionType.values());
    }
    
    @Override
    protected void prepareEditForm(Model model, Session entity) {
        model.addAttribute("sessionTypes", Session.SessionType.values());
    }
    
    @GetMapping
    public String listSessions(Model model) {
        return list(model, sessionService.findAll());
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        return showCreateForm(model, Session::new);
    }
    
    @PostMapping
    public String createSession(@ModelAttribute Session session, RedirectAttributes redirectAttributes) {
        return handleCreate(() -> sessionService.save(session), redirectAttributes);
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return showEditForm(id, model, sessionService.findById(id));
    }
    
    @PostMapping("/{id}")
    public String updateSession(@PathVariable Long id, @ModelAttribute Session session, 
                               RedirectAttributes redirectAttributes) {
        return handleUpdate(() -> sessionService.update(id, session), redirectAttributes);
    }
    
    @GetMapping("/{id}/delete")
    public String deleteSession(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return handleDelete(() -> sessionService.delete(id), redirectAttributes);
    }
}

