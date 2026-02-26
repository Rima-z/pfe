package com.projet1.auth_service.controller;

import com.projet1.auth_service.domain.User;
import com.projet1.auth_service.security.JwtUtil;
import com.projet1.auth_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    record SignupRequest(String username, String email, String password, Set<String> roles) {}
    record LoginRequest(String username, String password) {}

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        Set<String> roles = req.roles == null || req.roles.isEmpty() ? Set.of("ROLE_OCCUPANT") : req.roles.stream().map(r -> r.startsWith("ROLE_")? r : "ROLE_"+r.toUpperCase()).collect(Collectors.toSet());
        User u = userService.register(req.username, req.email, req.password, roles);

        // Création automatique du profil dans user-service avec champs vides si besoin
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String userServiceUrl = "http://localhost:8081/api/profiles";
            java.util.Map<String, String> profileData = java.util.Map.of(
                "username", u.getUsername(),
                "email", u.getEmail(),
                "firstName", "",
                "lastName", "",
                "phone", "",
                "address", ""
            );
            restTemplate.postForEntity(userServiceUrl, profileData, Void.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(java.util.Map.of("id", u.getId(), "username", u.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.username, req.password));
        var user = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        Set<String> roles = user.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toSet());
        String token = jwtUtil.generateToken(user.getUsername(), roles);
        return ResponseEntity.ok(Map.of("access_token", token, "token_type", "bearer"));
    }
}
