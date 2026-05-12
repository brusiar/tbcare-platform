package com.tbcare.users.repository;

import com.tbcare.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByTenantIdAndActiveTrue(UUID tenantId);
    Optional<User> findByEmailAndActiveTrue(String email);
}
