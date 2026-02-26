package com.digitaltwin.user_service.init;

import com.digitaltwin.user_service.domain.Building;
import com.digitaltwin.user_service.domain.Role;
import com.digitaltwin.user_service.repository.BuildingRepository;
import com.digitaltwin.user_service.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final BuildingRepository buildingRepository;

    public DataInitializer(RoleRepository roleRepository, BuildingRepository buildingRepository) {
        this.roleRepository = roleRepository;
        this.buildingRepository = buildingRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> roleNames = List.of("USER", "ADMIN", "MANAGER");
        for (String r : roleNames) {
            roleRepository.findByName(r).orElseGet(() -> roleRepository.save(new Role(null, r, null)));
        }

        if (buildingRepository.count() == 0) {
            buildingRepository.save(new Building(null, "HQ", "123 Main St", null));
            buildingRepository.save(new Building(null, "Warehouse", "456 Industrial Rd", null));
        }
    }
}
