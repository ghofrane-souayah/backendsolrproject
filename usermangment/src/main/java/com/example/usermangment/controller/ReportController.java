package com.example.usermangment.controller;

import com.example.usermangment.model.Report;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.UserRepository;
import com.example.usermangment.service.ReportService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    public ReportController(
            ReportService reportService,
            UserRepository userRepository
    ) {
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    @GetMapping
    public List<Report> getAll(Authentication auth) {
        return reportService.findAllForUser(currentUser(auth));
    }

    @PostMapping
    public void create(@RequestBody Report item, Authentication auth) {
        User user = currentUser(auth);

        if (item.getCompanyId() == null && user.getCompany() != null) {
            item.setCompanyId(user.getCompany().getId());
        }

        reportService.create(item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        reportService.delete(id);
    }
}