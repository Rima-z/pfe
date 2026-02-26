package com.digitaltwin.user_service.repository;

import com.digitaltwin.user_service.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
