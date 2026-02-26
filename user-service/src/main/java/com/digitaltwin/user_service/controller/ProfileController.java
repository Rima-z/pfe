package com.digitaltwin.user_service.controller;

import com.digitaltwin.user_service.domain.Profile;
import com.digitaltwin.user_service.domain.User;
import com.digitaltwin.user_service.repository.ProfileRepository;
import com.digitaltwin.user_service.service.UserService;
import com.digitaltwin.user_service.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileRepository profileRepository;
    private final com.digitaltwin.user_service.repository.UserRepository userRepository;
    private final UserService userService;
    private final RoleService roleService;

    public ProfileController(ProfileRepository profileRepository, 
                            com.digitaltwin.user_service.repository.UserRepository userRepository,
                            UserService userService,
                            RoleService roleService) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public List<Profile> getAll() {
        return profileRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profile> getOne(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Profile> create(@RequestBody java.util.Map<String, Object> payload) {
        // Vérifier si username et email sont présents (appel depuis auth-service)
        if (payload.containsKey("username") && payload.containsKey("email")) {
            // Créer User + Profile depuis auth-service
            User user = new User();
            user.setUsername((String) payload.get("username"));
            user.setEmail((String) payload.get("email"));

            Profile profile = new Profile();
            profile.setFirstName((String) payload.getOrDefault("firstName", ""));
            profile.setLastName((String) payload.getOrDefault("lastName", ""));
            profile.setPhone((String) payload.getOrDefault("phone", ""));
            profile.setAddress((String) payload.getOrDefault("address", ""));

            user.setProfile(profile);
            profile.setUser(user);

            // Ajout des rôles si présents dans le payload
            if (payload.containsKey("roles")) {
                java.util.List<String> roleNames = (java.util.List<String>) payload.get("roles");
                java.util.Set<com.digitaltwin.user_service.domain.Role> roles = new java.util.HashSet<>();
                for (String roleName : roleNames) {
                    com.digitaltwin.user_service.domain.Role role = roleService.findByName(roleName)
                        .orElseGet(() -> roleService.save(new com.digitaltwin.user_service.domain.Role(null, roleName, null)));
                    roles.add(role);
                }
                user.setRoles(roles);
            }

            User saved = userService.save(user);
            return ResponseEntity.created(URI.create("/api/profiles/" + saved.getProfile().getId())).body(saved.getProfile());
        } else {
            // Créer seulement un Profile (comportement original)
            Profile profile = new Profile();
            profile.setFirstName((String) payload.getOrDefault("firstName", ""));
            profile.setLastName((String) payload.getOrDefault("lastName", ""));
            profile.setPhone((String) payload.getOrDefault("phone", ""));
            profile.setAddress((String) payload.getOrDefault("address", ""));
            Profile saved = profileRepository.save(profile);
            return ResponseEntity.created(URI.create("/api/profiles/" + saved.getId())).body(saved);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profile> update(@PathVariable Long id, @RequestBody Profile profile) {
        return profileRepository.findById(id)
                .map(existing -> {
                    profile.setId(existing.getId());
                    return ResponseEntity.ok(profileRepository.save(profile));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (profileRepository.findById(id).isPresent()) {
            profileRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
