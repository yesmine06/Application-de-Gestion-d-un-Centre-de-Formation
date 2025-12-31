package com.formation.controller.admin;

import com.formation.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    
    private final StatisticsService statisticsService;
    
    public AdminDashboardController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }
    
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        Map<String, Object> stats = statisticsService.getAdminStatistics();
        model.addAttribute("stats", stats);
        model.addAttribute("mostFollowedCourses", statisticsService.getMostFollowedCourses(5));
        return "admin/dashboard";
    }
}

