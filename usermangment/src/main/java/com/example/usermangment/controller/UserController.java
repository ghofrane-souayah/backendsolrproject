package com.example.usermangment.controller;

import com.example.usermangment.dto.ChangePasswordRequest;
import com.example.usermangment.dto.CreateUserRequest;
import com.example.usermangment.dto.UpdateUserRequest;
import com.example.usermangment.dto.UserDto;
import com.example.usermangment.dto.UserStatsDto;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.UserRepository;
import com.example.usermangment.service.NotificationService;
import com.example.usermangment.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public UserController(
            UserRepository userRepository,
            UserService userService,
            NotificationService notificationService
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    private User currentUser(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    @GetMapping
    public List<UserDto> list(
            @RequestParam(required = false) Long companyId,
            Authentication auth
    ) {
        return userService.listFor(currentUser(auth), companyId);
    }

    @PostMapping
    public UserDto create(@RequestBody CreateUserRequest req, Authentication auth) {
        User current = currentUser(auth);
        Long companyId = current.getCompany() != null ? current.getCompany().getId() : null;

        try {
            UserDto result = userService.createForTenant(req, current);

            String label = result.getEmail();

            notificationService.createSuccess(
                    "Utilisateur créé",
                    "L'utilisateur " + label + " a été créé avec succès.",
                    "Users",
                    companyId
            );

            return result;
        } catch (Exception e) {
            notificationService.createError(
                    "Échec création utilisateur",
                    e.getMessage() != null ? e.getMessage() : "La création de l'utilisateur a échoué.",
                    "Users",
                    companyId
            );
            throw e;
        }
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UpdateUserRequest req, Authentication auth) {
        User current = currentUser(auth);
        Long companyId = current.getCompany() != null ? current.getCompany().getId() : null;

        try {
            UserDto result = userService.updateForTenant(id, req, current);

            String label = result.getEmail();

            notificationService.createSuccess(
                    "Utilisateur modifié",
                    "L'utilisateur " + label + " a été modifié avec succès.",
                    "Users",
                    companyId
            );

            return result;
        } catch (Exception e) {
            notificationService.createError(
                    "Échec modification utilisateur",
                    e.getMessage() != null ? e.getMessage() : "La modification de l'utilisateur a échoué.",
                    "Users",
                    companyId
            );
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        User current = currentUser(auth);
        Long companyId = current.getCompany() != null ? current.getCompany().getId() : null;

        try {
            userService.deleteForTenant(id, current);

            notificationService.createSuccess(
                    "Utilisateur supprimé",
                    "L'utilisateur avec l'id " + id + " a été supprimé avec succès.",
                    "Users",
                    companyId
            );
        } catch (Exception e) {
            notificationService.createError(
                    "Échec suppression utilisateur",
                    e.getMessage() != null ? e.getMessage() : "La suppression de l'utilisateur a échoué.",
                    "Users",
                    companyId
            );
            throw e;
        }
    }

    @GetMapping("/stats")
    public UserStatsDto stats(@RequestParam(required = false) Long companyId, Authentication auth) {
        return userService.statsFor(currentUser(auth), companyId);
    }

    @GetMapping("/me")
    public UserDto me(Authentication auth) {
        return userService.toDtoSafe(currentUser(auth));
    }

    @PostMapping("/me/password")
    public void changeMyPassword(@RequestBody ChangePasswordRequest req, Authentication auth) {
        User current = currentUser(auth);
        Long companyId = current.getCompany() != null ? current.getCompany().getId() : null;

        try {
            userService.changeMyPassword(req, current);

            notificationService.createSuccess(
                    "Mot de passe modifié",
                    "Le mot de passe a été modifié avec succès.",
                    "Users",
                    companyId
            );
        } catch (Exception e) {
            notificationService.createError(
                    "Échec modification mot de passe",
                    e.getMessage() != null ? e.getMessage() : "La modification du mot de passe a échoué.",
                    "Users",
                    companyId
            );
            throw e;
        }
    }
}