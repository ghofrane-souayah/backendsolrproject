package com.example.usermangment.controller;

import com.example.usermangment.dto.CreateUserRequest;
import com.example.usermangment.dto.UpdateUserRequest;
import com.example.usermangment.dto.UserDto;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.UserRepository;
import com.example.usermangment.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.usermangment.dto.UserStatsDto;
import com.example.usermangment.dto.ChangePasswordRequest;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
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
        return userService.createForTenant(req, currentUser(auth));
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UpdateUserRequest req, Authentication auth) {
        return userService.updateForTenant(id, req, currentUser(auth));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        userService.deleteForTenant(id, currentUser(auth));
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
        userService.changeMyPassword(req, currentUser(auth));
    }

}
