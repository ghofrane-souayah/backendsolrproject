package com.example.usermangment.controller;

import com.example.usermangment.model.Notifications;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.UserRepository;
import com.example.usermangment.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationService notificationService,
            UserRepository userRepository
    ) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    @GetMapping
    public List<Notifications> getAll(Authentication auth) {
        return notificationService.findAllForUser(currentUser(auth));
    }

    @PostMapping
    public void create(@RequestBody Notifications item, Authentication auth) {
        if (item.getStatus() == null || item.getStatus().isBlank()) {
            item.setStatus("unread");
        }

        User user = currentUser(auth);

        if (item.getCompanyId() == null && user.getCompany() != null) {
            item.setCompanyId(user.getCompany().getId());
        }

        notificationService.create(item);
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    @PatchMapping("/read-all")
    public void markAllAsRead() {
        notificationService.markAllAsRead();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        notificationService.delete(id);
    }
}