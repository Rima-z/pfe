package com.digitaltwin.user_service.controller;

import com.digitaltwin.user_service.domain.Building;
import com.digitaltwin.user_service.service.BuildingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/buildings")
public class BuildingController {
    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @GetMapping
    public List<Building> getAll() {
        return buildingService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Building> getOne(@PathVariable Long id) {
        return buildingService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Building> create(@RequestBody Building building) {
        Building saved = buildingService.save(building);
        return ResponseEntity.created(URI.create("/api/buildings/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Building> update(@PathVariable Long id, @RequestBody Building building) {
        return buildingService.findById(id)
                .map(existing -> {
                    building.setId(existing.getId());
                    return ResponseEntity.ok(buildingService.save(building));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (buildingService.findById(id).isPresent()) {
            buildingService.delete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
