package com.digitaltwin.user_service.repository;

import com.digitaltwin.user_service.domain.Building;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long> {
}
