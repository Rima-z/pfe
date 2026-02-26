package com.projet1.auth_service.controller;

import com.projet1.auth_service.domain.User;
import com.projet1.auth_service.security.JwtUtil;
import com.projet1.auth_service.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    record SignupRequest(String username, String email, String password, Set<String> roles) {}
    record LoginRequest(String username, String password) {}

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private static final String USER_SERVICE_URL = "http://localhost:8081/api/profiles";

    public AuthController(UserService userService, AuthenticationManager authenticationManager, 
                         JwtUtil jwtUtil, RestTemplate restTemplate) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        Set<String> roles = req.roles == null || req.roles.isEmpty() ? Set.of("ROLE_OCCUPANT") : req.roles.stream().map(r -> r.startsWith("ROLE_")? r : "ROLE_"+r.toUpperCase()).collect(Collectors.toSet());
        User u = userService.register(req.username, req.email, req.password, roles);

        // Appel user-service pour créer automatiquement le profil et l'utilisateur dans userdb
        try {
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("username", u.getUsername());
            profileData.put("email", u.getEmail());
            profileData.put("firstName", "");
            profileData.put("lastName", "");
            profileData.put("phone", "");
            profileData.put("address", "");
            profileData.put("roles", new ArrayList<>(roles));
            
            logger.info("Création du profil et de l'utilisateur dans user-service pour: {}", u.getUsername());
            restTemplate.postForEntity(USER_SERVICE_URL, profileData, Object.class);
            logger.info("Profil et utilisateur créés avec succès dans user-service pour: {}", u.getUsername());
        } catch (RestClientException e) {
            logger.error("Erreur lors de la création du profil dans user-service pour {}: {}", u.getUsername(), e.getMessage());
            // On continue quand même car l'utilisateur est créé dans auth-service
            // Dans un environnement de production, on pourrait vouloir rollback la transaction
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'appel à user-service pour {}: {}", u.getUsername(), e.getMessage(), e);
        }

        return ResponseEntity.ok(Map.of("id", u.getId(), "username", u.getUsername()));
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
