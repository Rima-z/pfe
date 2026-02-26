package com.digitaltwin.user_service.controller;

import com.digitaltwin.user_service.domain.Profile;
import com.digitaltwin.user_service.repository.ProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileRepository profileRepository;
    private final com.digitaltwin.user_service.repository.UserRepository userRepository;

    public ProfileController(ProfileRepository profileRepository, com.digitaltwin.user_service.repository.UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
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
    public ResponseEntity<Profile> create(@RequestBody java.util.Map<String, String> payload) {
        Profile profile = new Profile();
        profile.setFirstName(payload.getOrDefault("firstName", ""));
        profile.setLastName(payload.getOrDefault("lastName", ""));
        profile.setPhone(payload.getOrDefault("phone", ""));
        profile.setAddress(payload.getOrDefault("address", ""));
        // Optionnel : stocker username/email dans des champs additionnels si besoin
        Profile saved = profileRepository.save(profile);
        return ResponseEntity.created(URI.create("/api/profiles/" + saved.getId())).body(saved);
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
