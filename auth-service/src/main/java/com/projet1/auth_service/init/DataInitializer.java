package com.projet1.auth_service.init;

import com.projet1.auth_service.domain.Role;
import com.projet1.auth_service.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> defaults = List.of("ROLE_EXPLOITANT", "ROLE_MAINTENANCE", "ROLE_DIRECTION", "ROLE_OCCUPANT");
        for (String r : defaults) {
            roleRepository.findByName(r).orElseGet(() -> roleRepository.save(new Role(r)));
        }
    }
}
