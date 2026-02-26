package org.example.like_hero_to_zero.controller;

import org.springframework.security.core.Authentication;
import org.example.like_hero_to_zero.repository.EmissionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EmissionRepository emissionRepository;

    public AdminController(EmissionRepository emissionRepository) {
        this.emissionRepository = emissionRepository;
    }

    @GetMapping("/pending")
    public String pendingEmissions(Model model, Authentication authentication) {
        model.addAttribute("emissions", emissionRepository.findPendingEmissions());
        model.addAttribute("username", authentication.getName());
        return "admin-pending";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        emissionRepository.findById(id).ifPresent(e -> {
            e.setStatus("APPROVED");
            emissionRepository.save(e);
        });
        ra.addFlashAttribute("success", "Datensatz freigegeben!");
        return "redirect:/admin/pending";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {
        emissionRepository.findById(id).ifPresent(e -> {
            e.setStatus("REJECTED");
            emissionRepository.save(e);
        });
        ra.addFlashAttribute("error", "Datensatz abgelehnt!");
        return "redirect:/admin/pending";
    }
}