
package com.digitaltwin.user_service.controller;

import com.digitaltwin.user_service.domain.User;
import com.digitaltwin.user_service.domain.Profile;
import com.digitaltwin.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final com.digitaltwin.user_service.service.BuildingService buildingService;
    private final com.digitaltwin.user_service.service.RoleService roleService;

    public UserController(UserService userService, com.digitaltwin.user_service.service.BuildingService buildingService,
                          com.digitaltwin.user_service.service.RoleService roleService) {
        this.userService = userService;
        this.buildingService = buildingService;
        this.roleService = roleService;
    }
    @PostMapping("/register")
    public ResponseEntity<User> registerWithProfile(@RequestBody java.util.Map<String, String> payload) {
        User user = new User();
        user.setUsername(payload.get("username"));
        user.setEmail(payload.get("email"));
        // autres champs utilisateur si besoin

        Profile profile = new Profile();
        profile.setFirstName(payload.getOrDefault("firstName", ""));
        profile.setLastName(payload.getOrDefault("lastName", ""));
        profile.setPhone(payload.getOrDefault("phone", ""));
        profile.setAddress(payload.getOrDefault("address", ""));

        user.setProfile(profile);
        profile.setUser(user);

        User saved = userService.save(user);
        return ResponseEntity.created(java.net.URI.create("/api/users/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getOne(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        User saved = userService.save(user);
        return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User user) {
        return userService.findById(id)
                .map(existing -> {
                    user.setId(existing.getId());
                    return ResponseEntity.ok(userService.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (userService.findById(id).isPresent()) {
            userService.delete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ----- helper endpoints for assignments -----

    @PostMapping("/{id}/buildings/{buildingId}")
    public ResponseEntity<User> addBuilding(@PathVariable Long id, @PathVariable Long buildingId) {
        return userService.findById(id).flatMap(user ->
                buildingService.findById(buildingId).map(building -> {
                    user.getBuildings().add(building);
                    return ResponseEntity.ok(userService.save(user));
                })
        ).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/buildings/{buildingId}")
    public ResponseEntity<User> removeBuilding(@PathVariable Long id, @PathVariable Long buildingId) {
        return userService.findById(id).map(user -> {
            user.getBuildings().removeIf(b -> b.getId().equals(buildingId));
            return ResponseEntity.ok(userService.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/roles/{roleId}")
    public ResponseEntity<User> addRole(@PathVariable Long id, @PathVariable Long roleId) {
        return userService.findById(id).flatMap(user ->
                roleService.findById(roleId).map(role -> {
                    user.getRoles().add(role);
                    return ResponseEntity.ok(userService.save(user));
                })
        ).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    public ResponseEntity<User> removeRole(@PathVariable Long id, @PathVariable Long roleId) {
        return userService.findById(id).map(user -> {
            user.getRoles().removeIf(r -> r.getId().equals(roleId));
            return ResponseEntity.ok(userService.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }
}
