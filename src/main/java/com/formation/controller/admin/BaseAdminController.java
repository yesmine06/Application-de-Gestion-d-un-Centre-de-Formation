package com.formation.controller.admin;

import com.formation.controller.BaseController;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.function.Supplier;

/**
 * Contrôleur de base pour les opérations CRUD admin communes
 * Principe SOLID : DRY (Don't Repeat Yourself) et Open/Closed
 * 
 * @param <T> Type de l'entité
 * @param <ID> Type de l'ID
 */
public abstract class BaseAdminController<T, ID> extends BaseController {
    
    protected final String entityName;
    protected final String entityNamePlural;
    protected final String basePath;
    
    protected BaseAdminController(String entityName, String entityNamePlural, String basePath) {
        this.entityName = entityName;
        this.entityNamePlural = entityNamePlural;
        this.basePath = basePath;
    }
    
    /**
     * Liste toutes les entités
     */
    protected String list(Model model, java.util.List<T> entities) {
        model.addAttribute(entityNamePlural, entities);
        return "admin/" + basePath + "/list";
    }
    
    /**
     * Affiche le formulaire de création
     */
    protected String showCreateForm(Model model, Supplier<T> entitySupplier) {
        model.addAttribute(entityName.toLowerCase(), entitySupplier.get());
        prepareCreateForm(model);
        return "admin/" + basePath + "/form";
    }
    
    /**
     * Affiche le formulaire d'édition
     */
    protected String showEditForm(ID id, Model model, java.util.Optional<T> entity) {
        entity.ifPresent(e -> {
            model.addAttribute(entityName.toLowerCase(), e);
            prepareEditForm(model, e);
        });
        return "admin/" + basePath + "/form";
    }
    
    /**
     * Gère la création avec messages de succès/erreur
     */
    protected String handleCreate(Runnable saveAction, RedirectAttributes redirectAttributes) {
        try {
            saveAction.run();
            redirectAttributes.addFlashAttribute("success", 
                capitalize(entityName) + " créé(e) avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la création: " + e.getMessage());
        }
        return "redirect:/admin/" + basePath;
    }
    
    /**
     * Gère la mise à jour avec messages de succès/erreur
     */
    protected String handleUpdate(Runnable updateAction, RedirectAttributes redirectAttributes) {
        try {
            updateAction.run();
            redirectAttributes.addFlashAttribute("success", 
                capitalize(entityName) + " mis(e) à jour avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la mise à jour: " + e.getMessage());
        }
        return "redirect:/admin/" + basePath;
    }
    
    /**
     * Gère la suppression avec messages de succès/erreur
     */
    protected String handleDelete(Runnable deleteAction, RedirectAttributes redirectAttributes) {
        try {
            deleteAction.run();
            redirectAttributes.addFlashAttribute("success", 
                capitalize(entityName) + " supprimé(e) avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/admin/" + basePath;
    }
    
    /**
     * Prépare le formulaire de création (peut être surchargé)
     */
    protected void prepareCreateForm(Model model) {
        // À surcharger si nécessaire pour ajouter des données supplémentaires
    }
    
    /**
     * Prépare le formulaire d'édition (peut être surchargé)
     */
    protected void prepareEditForm(Model model, T entity) {
        // À surcharger si nécessaire pour ajouter des données supplémentaires
    }
    
    /**
     * Capitalise la première lettre d'une chaîne
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
