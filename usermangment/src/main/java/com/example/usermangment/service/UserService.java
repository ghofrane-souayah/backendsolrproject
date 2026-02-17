package com.example.usermangment.service;

import com.example.usermangment.dto.CreateUserRequest;
import com.example.usermangment.dto.UpdateUserRequest;
import com.example.usermangment.dto.UserDto;
import com.example.usermangment.model.Company;
import com.example.usermangment.model.Role;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.CompanyRepository;
import com.example.usermangment.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.usermangment.dto.UserStatsDto;
import com.example.usermangment.dto.ChangePasswordRequest;
import org.springframework.security.authentication.BadCredentialsException;
import java.util.List;
@Service
public class UserService {

    private final UserRepository repo;
    private final CompanyRepository companyRepo;
    private final PasswordEncoder encoder;


    public UserService(UserRepository repo, CompanyRepository companyRepo, PasswordEncoder encoder) {
        this.repo = repo;
        this.companyRepo = companyRepo;
        this.encoder = encoder;
    }

    // ✅ DTO safe (company + role string)
    public UserDto toDtoSafe(User u) {
        return new UserDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole().name(),
                u.isEnabled(),
                u.getCompany() != null ? u.getCompany().getId() : null,
                u.getCompany() != null ? u.getCompany().getName() : null
        );
    }

    // ✅ CREATE tenant-aware (LE PLUS IMPORTANT)
    public UserDto createForTenant(CreateUserRequest req, User current) {

        // 1) sécurité rôle
        if (current.getRole() == Role.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }

        // 2) champs
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        String email = req.getEmail() == null ? "" : req.getEmail().trim().toLowerCase();
        String password = req.getPassword() == null ? "" : req.getPassword();

        if (username.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is required");
        if (email.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        if (password.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");

        if (repo.existsByUsername(username)) throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
        if (repo.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "email already exists");

        // 3) role demandé
        Role requestedRole = (req.getRole() != null) ? req.getRole() : Role.USER;

        // ✅ ADMIN ne peut jamais créer SUPER_ADMIN
        if (current.getRole() == Role.ADMIN && requestedRole == Role.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ADMIN cannot create SUPER_ADMIN");
        }

        // 4) company cible
        Company targetCompany;

        if (current.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN choisit via companyId (obligatoire)
            if (req.getCompanyId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "companyId is required for SUPER_ADMIN");
            }
            targetCompany = companyRepo.findById(req.getCompanyId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "company not found"));
        } else {
            // ADMIN -> on force SA company (on ignore companyId venant du front)
            if (current.getCompany() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "current admin has no company");
            }
            targetCompany = current.getCompany();
        }

        // 5) create entity
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setRole(requestedRole);
        user.setEnabled(req.isEnabled());
        user.setCompany(targetCompany);

        return toDtoSafe(repo.save(user));
    }

    // ✅ UPDATE tenant-aware (ADMIN: même company, SUPER_ADMIN: tout)
    public UserDto updateForTenant(Long id, UpdateUserRequest req, User current) {

        // 1) sécurité : USER interdit
        if (current.getRole() == Role.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }

        // 2) target à modifier
        User target = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        // 3) ADMIN : même company obligatoire
        if (current.getRole() == Role.ADMIN) {
            if (current.getCompany() == null || target.getCompany() == null
                    || !current.getCompany().getId().equals(target.getCompany().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your company");
            }
        }

        // 4) ADMIN ne peut jamais modifier un SUPER_ADMIN
        if (current.getRole() == Role.ADMIN && target.getRole() == Role.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "cannot modify SUPER_ADMIN");
        }

        // 5) champs obligatoires
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        String email = req.getEmail() == null ? "" : req.getEmail().trim().toLowerCase();

        if (username.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is required");
        if (email.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");

        // 6) unicité username/email
        repo.findByUsername(username).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
            }
        });

        repo.findByEmail(email).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "email already exists");
            }
        });

        // 7) rôle demandé (si vide => garder l’ancien)
        Role newRole = (req.getRole() != null) ? req.getRole() : target.getRole();

        // 8) ADMIN ne peut pas promouvoir vers SUPER_ADMIN
        if (current.getRole() == Role.ADMIN && newRole == Role.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ADMIN cannot set SUPER_ADMIN");
        }

        // 9) ADMIN ne peut pas se dégrader lui-même (ADMIN -> USER)
        if (current.getRole() == Role.ADMIN
                && target.getId().equals(current.getId())
                && newRole == Role.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin cannot downgrade self");
        }

        // 10) (Option PRO stricte) Interdire de changer le rôle d’un SUPER_ADMIN
        //     Même SUPER_ADMIN ne peut pas le downgrade (évite erreurs)
        if (target.getRole() == Role.SUPER_ADMIN && newRole != Role.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "cannot downgrade SUPER_ADMIN");
        }

        // 11) appliquer les changements
        target.setUsername(username);
        target.setEmail(email);
        target.setRole(newRole);
        target.setEnabled(req.isEnabled());

        // 12) password optionnel
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            target.setPassword(encoder.encode(req.getPassword()));
        }

        return toDtoSafe(repo.save(target));
    }


    // ✅ DELETE tenant-aware (ADMIN: même company, SUPER_ADMIN: tout)
    public void deleteForTenant(Long id, User current) {

        // 1) USER interdit
        if (current.getRole() == Role.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }

        User target = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        // 2) Personne ne peut se supprimer soi-même
        if (target.getId().equals(current.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "cannot delete yourself");
        }

        // 3) ADMIN règles
        if (current.getRole() == Role.ADMIN) {

            // ADMIN ne peut pas supprimer SUPER_ADMIN
            if (target.getRole() == Role.SUPER_ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "cannot delete SUPER_ADMIN");
            }

            // ADMIN : même company obligatoire
            if (current.getCompany() == null || target.getCompany() == null
                    || !current.getCompany().getId().equals(target.getCompany().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your company");
            }
        }

        // 4) SUPER_ADMIN peut tout (sauf lui-même déjà bloqué)
        repo.delete(target);
    }



    public List<UserDto> listFor(User current, Long companyId) {

        List<User> users;

        if (current.getRole() == Role.SUPER_ADMIN) {
            // ✅ SUPER_ADMIN : si companyId null => tout, sinon => filtré
            if (companyId == null) {
                users = repo.findAll();
            } else {
                users = repo.findAllByCompany_Id(companyId);
            }

        } else if (current.getRole() == Role.ADMIN) {
            // ✅ ADMIN : ignore companyId venant du front, forcé sur sa company
            if (current.getCompany() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "current admin has no company");
            }
            users = repo.findAllByCompany_Id(current.getCompany().getId());

        } else {
            // USER: seulement lui-même (ou FORBIDDEN si tu veux)
            users = List.of(current);
        }

        return users.stream().map(this::toDtoSafe).toList();
    }
    public UserStatsDto statsFor(User current, Long companyId) {

        // SUPER_ADMIN: global ou par company
        if (current.getRole() == Role.SUPER_ADMIN) {
            if (companyId == null) {
                long active = repo.countByEnabledTrue();
                long inactive = repo.countByEnabledFalse();
                return new UserStatsDto(active + inactive, active, inactive);
            } else {
                long active = repo.countByCompany_IdAndEnabledTrue(companyId);
                long inactive = repo.countByCompany_IdAndEnabledFalse(companyId);
                return new UserStatsDto(active + inactive, active, inactive);
            }
        }

        // ADMIN: forcé sur sa company (ignore companyId)
        if (current.getRole() == Role.ADMIN) {
            if (current.getCompany() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "current admin has no company");
            }
            Long cid = current.getCompany().getId();
            long active = repo.countByCompany_IdAndEnabledTrue(cid);
            long inactive = repo.countByCompany_IdAndEnabledFalse(cid);
            return new UserStatsDto(active + inactive, active, inactive);
        }

        // USER: juste lui-même
        long active = current.isEnabled() ? 1 : 0;
        long inactive = current.isEnabled() ? 0 : 1;
        return new UserStatsDto(1, active, inactive);
    }


    public void changeMyPassword(ChangePasswordRequest req, User current) {
        if (req.getOldPassword() == null || req.getOldPassword().isBlank()
                || req.getNewPassword() == null || req.getNewPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "oldPassword/newPassword required");
        }

        if (!encoder.matches(req.getOldPassword(), current.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "old password incorrect");
        }

        current.setPassword(encoder.encode(req.getNewPassword()));
        repo.save(current);
    }


}
